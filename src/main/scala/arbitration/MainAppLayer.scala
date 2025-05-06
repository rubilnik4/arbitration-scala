package arbitration

import arbitration.api.routes.{RoutesLayer, ServerLayer}
import arbitration.application.commands.handlers.MarketCommandHandlerLayer
import arbitration.application.configurations.AppConfig
import arbitration.application.environments.*
import arbitration.application.jobs.SpreadJobLayer
import arbitration.application.metrics.MarketMetricsLayer
import arbitration.application.queries.handlers.MarketQueryHandlerLayer
import arbitration.application.queries.marketData.MarketDataLayer
import arbitration.application.telemetry.TelemetryLayer
import arbitration.infrastructure.caches.MarketCacheLayer
import arbitration.infrastructure.markets.BinanceMarketApiLayer
import arbitration.infrastructure.repositories.PostgresMarketRepositoryLayer
import zio.*
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter

object MainAppLayer {
  private val appLive: ZLayer[AppConfig & Meter, Throwable, AppEnv] = {
    val repositoryLayer = PostgresMarketRepositoryLayer.postgresMarketRepositoryLive
    val cacheLayer = repositoryLayer >>> MarketCacheLayer.marketCacheLive
    val combinedLayers =
      MarketMetricsLayer.MarketMetricsLive ++
        repositoryLayer ++
        cacheLayer ++
        BinanceMarketApiLayer.binanceMarketApiLive ++
        MarketDataLayer.marketDataLive ++
        MarketQueryHandlerLayer.marketQueryHandlerLive ++
        MarketCommandHandlerLayer.marketCommandHandlerLive
    combinedLayers >>> AppEnvLayer.appEnvLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server & Unit] =
    AppConfigLayer.appConfigLive >>>
      (TelemetryLayer.telemetryLive >>>
        (appLive >>>
          (RoutesLayer.apiRoutesLive >>> ServerLayer.serverLive) ++ SpreadJobLayer.spreadJobLive))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}