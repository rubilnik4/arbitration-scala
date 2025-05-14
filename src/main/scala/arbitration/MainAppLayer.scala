package arbitration

import arbitration.api.routes.{RoutesLayer, ServerLayer}
import arbitration.application.commands.handlers.MarketCommandHandlerLayer
import arbitration.application.configurations.AppConfig
import arbitration.application.jobs.SpreadJobLayer
import arbitration.application.queries.handlers.MarketQueryHandlerLayer
import arbitration.application.queries.marketData.MarketDataLayer
import arbitration.application.telemetry.metrics.MarketMeterLayer
import arbitration.application.telemetry.tracing.MarketTracingLayer
import arbitration.infrastructure.caches.MarketCacheLayer
import arbitration.infrastructure.markets.BinanceMarketApiLayer
import arbitration.infrastructure.repositories.PostgresMarketRepositoryLayer
import arbitration.infrastructure.telemetry.TelemetryLayer
import arbitration.layers.{AppConfigLayer, AppEnv, AppEnvLayer}
import io.opentelemetry.context.propagation.TextMapPropagator
import zio.*
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object MainAppLayer {
  private val appLive: ZLayer[AppConfig & Meter & Tracing, Throwable, AppEnv] = {
    val repositoryLayer = PostgresMarketRepositoryLayer.postgresMarketRepositoryLive
    val cacheLayer = repositoryLayer >>> MarketCacheLayer.marketCacheLive
    val combinedLayers =
      MarketMeterLayer.marketMeterLive ++
        MarketTracingLayer.marketTracingLive ++
        repositoryLayer ++
        cacheLayer ++
        BinanceMarketApiLayer.binanceMarketApiLive ++
        MarketDataLayer.marketDataLive ++
        MarketQueryHandlerLayer.marketQueryHandlerLive ++
        MarketCommandHandlerLayer.marketCommandHandlerLive
    combinedLayers >>> AppEnvLayer.appEnvLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
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