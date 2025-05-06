package arbitration

import arbitration.api.routes.{RoutesLayer, ServerLayer}
import arbitration.application.commands.handlers.MarketCommandHandlerLayer
import arbitration.application.environments.*
import arbitration.application.jobs.SpreadJobLayer
import arbitration.application.logging.LoggingLayer
import arbitration.application.queries.handlers.MarketQueryHandlerLayer
import arbitration.application.queries.marketData.MarketDataLayer
import arbitration.infrastructure.caches.MarketCacheLayer
import arbitration.infrastructure.markets.BinanceMarketApiLayer
import arbitration.infrastructure.repositories.PostgresMarketRepositoryLayer
import zio.*

object MainAppLayer {
  private val appLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      AppConfigLayer.appConfigLive,
      PostgresMarketRepositoryLayer.postgresMarketRepositoryLive,
      MarketCacheLayer.marketCacheLive,
      MarketDataLayer.marketDataLive,
      BinanceMarketApiLayer.binanceMarketApiLive,
      MarketQueryHandlerLayer.marketQueryHandlerLive,
      MarketCommandHandlerLayer.marketCommandHandlerLive,
      AppEnvLayer.appEnvLive
    )

  private val runtimeLive =
    appLive >>>
      (LoggingLayer.telemetryLive >>>
        (RoutesLayer.apiRoutesLive >>> ServerLayer.serverLive) ++ SpreadJobLayer.spreadJobLive)



  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}