package arbitration.application.environments

import arbitration.application.commands.handlers.MarketCommandHandler
import arbitration.application.configurations.AppConfig
import arbitration.application.metrics.MarketMeter
import arbitration.application.queries.handlers.MarketQueryHandler
import arbitration.application.queries.marketData.MarketData
import arbitration.infrastructure.caches.MarketCache
import arbitration.infrastructure.markets.MarketApi
import arbitration.infrastructure.repositories.MarketRepository
import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      with MarketCache with MarketRepository with MarketData
      with MarketApi with MarketQueryHandler with MarketCommandHandler
      with MarketMeter with Tracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
