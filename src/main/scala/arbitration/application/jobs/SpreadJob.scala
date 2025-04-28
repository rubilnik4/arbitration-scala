package arbitration.application.jobs

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.domain.models.{AssetId, AssetSpreadId, SpreadState}
import zio.{Cause, Fiber, ZIO}

class SpreadJob {
  private def computeSpread(state: SpreadState): ZIO[AppEnv, Nothing, SpreadState] =
    ZIO.serviceWithZIO[AppEnv] { env =>
      val spreadAssetId = AssetSpreadId(
        AssetId(env.appConfig.project.assets.assetA),
        AssetId(env.appConfig.project.assets.assetB)
      )

      ZIO.logSpan("arbitration.compute-spread")  {
        SpreadCommandHandlerLive().handle(SpreadCommand(state, spreadAssetId)).foldZIO(
          err =>
            ZIO.logAnnotate("result.status", "error") {
              ZIO.logErrorCause("Failed to compute spread", Cause.fail(err)).as(state)
            },
          result =>
            ZIO.logAnnotate("result.status", "success") {
              ZIO.logAnnotate("spread.value", result.spread.value.toString) {
                ZIO.logInfo(s"Spread computed: ${result.spread}").as(result.spreadState)
              }
            }
        )
      }
    }

  private def spreadJobLoop(initialState: SpreadState): ZIO[AppEnv, Nothing, Unit] =
    ZIO.iterate(initialState)(_ => true) { state =>
      for {
        env <- ZIO.service[AppEnv]
        _ <- ZIO.sleep(zio.Duration.fromScala(env.appConfig.project.assetLoadingDelay))
        newState <- computeSpread(state)
      } yield newState
    }.unit

  val startSpreadJob: ZIO[AppEnv, Nothing, Fiber.Runtime[Nothing, Unit]] =
    ZIO.logInfo("Starting spread job") *>
      ZIO.scoped {
        spreadJobLoop(SpreadState.Init())
          .forkDaemon
          .withFinalizer(_ => ZIO.logInfo("Spread job stopped"))
      }
}
