package arbitration.application.jobs

import arbitration.application.commands.commands.SpreadCommand
import arbitration.domain.models.{AssetId, AssetSpreadId, SpreadState}
import arbitration.layers.AppEnv
import io.opentelemetry.api.trace.SpanKind
import zio.*

object SpreadJob {
  private def computeSpread(state: SpreadState): ZIO[AppEnv, Nothing, SpreadState] =
    ZIO.serviceWithZIO[AppEnv] { env =>
      val spreadAssetId = AssetSpreadId(
        AssetId(env.appConfig.project.assets.assetA),
        AssetId(env.appConfig.project.assets.assetB)
      )

      val spreadLogic = for {
        _ <- ZIO.logInfo(s"Starting job spread computation for: $spreadAssetId")
        spreadState <- env.marketCommandHandler.spreadCommandHandler.handle(SpreadCommand(state, spreadAssetId))
          .foldZIO(
            err => ZIO.logErrorCause("Failed to compute spread", Cause.fail(err)).as(state),
            result => ZIO.logInfo(s"Spread computed: ${result.spread}").as(result.spreadState)
          )
      } yield spreadState

      spreadLogic
        @@ env.marketTracing.tracing.aspects.root("arbitration.compute-spread", SpanKind.INTERNAL)
        @@ env.marketMeter.aspects.spreadDuration
    }

  def spreadJob(initialState: SpreadState): ZIO[AppEnv, Nothing, Unit] =
    ZIO.iterate(initialState)(_ => true) { state =>
      for {
        env <- ZIO.service[AppEnv]
        _ <- ZIO.sleep(env.appConfig.project.assetLoadingDelay)
        newState <- computeSpread(state)
      } yield newState
    }.unit
}
