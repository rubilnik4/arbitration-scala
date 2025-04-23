package arbitration.jobs

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.domain.models.{AssetId, AssetSpreadId, SpreadState}
import zio.{Cause, Fiber, ZIO}

class SpreadJob {
  private def computeSpread(state: SpreadState): ZIO[AppEnv, Nothing, SpreadState] =
    ZIO.serviceWithZIO[AppEnv] { env =>
      //ZIO.logSpan("arbitration.compute-spread", "operation" -> "compute-spread") {
        val spreadAssetId = AssetSpreadId(
          AssetId(env.appConfig.project.assets.assetA),
          AssetId(env.appConfig.project.assets.assetB)
        )

        SpreadCommandHandlerLive().execute(SpreadCommand(state, spreadAssetId)).foldZIO(
          err =>
            ZIO.logErrorCause(s"Failed to compute spread", Cause.fail(err))
              .as(state),
          result =>
            ZIO.logAnnotate("spread.value", result.spread.value.toString) {
              ZIO.logDebug(s"Spread computed: ${result.spread}")
                .as(result.spreadState)
            }
        )
      //}
    }

  private def spreadJobLoop(initialState: SpreadState): ZIO[AppEnv, Nothing, Unit] =
    ZIO.logInfo("Starting spread job loop") *>
      ZIO.iterate(initialState)(_ => true) { state =>
        for {
          env <- ZIO.service[AppEnv]
          _ <- ZIO.sleep(zio.Duration.fromScala(env.appConfig.project.assetLoadingDelay))
          newState <- computeSpread(state)
        } yield newState
      }.unit

  val startSpreadJob: ZIO[AppEnv, Nothing, Fiber.Runtime[Nothing, Unit]] =
    spreadJobLoop(SpreadState.Init()).forkDaemon
}
