package arbitration.application.jobs

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.domain.models.{AssetId, AssetSpreadId, SpreadState}
import zio.{Cause, ZIO}

object SpreadJob {
  private def computeSpread(state: SpreadState): ZIO[AppEnv, Nothing, SpreadState] =
    ZIO.serviceWithZIO[AppEnv] { env =>
      val spreadAssetId = AssetSpreadId(
        AssetId(env.appConfig.project.assets.assetA),
        AssetId(env.appConfig.project.assets.assetB)
      )      
      val metrics = env.marketMetrics 
      
      ZIO.logSpan("arbitration.compute-spread") {
        metrics.recordSpreadDuration {
          for {           
            result <- SpreadCommandHandlerLive().handle(SpreadCommand(state, spreadAssetId)).foldZIO(
              err => ZIO.logErrorCause("Failed to compute spread", Cause.fail(err)).as(state),
              result => ZIO.logInfo(s"Spread computed: ${result.spread}").as(result.spreadState)
            )            
          } yield result
        }
      }
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
