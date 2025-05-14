package arbitration.layers

import arbitration.application.commands.handlers.MarketCommandHandler
import arbitration.application.configurations.AppConfig
import arbitration.application.queries.handlers.MarketQueryHandler
import arbitration.application.queries.marketData.MarketData
import arbitration.application.telemetry.metrics.MarketMeter
import arbitration.application.telemetry.tracing.MarketTracing
import arbitration.infrastructure.caches.MarketCache
import arbitration.infrastructure.markets.MarketApi
import arbitration.infrastructure.repositories.MarketRepository
import zio.telemetry.opentelemetry.tracing.Tracing

final case class AppEnvLive (
  appConfig: AppConfig,
  marketCache: MarketCache,
  marketRepository: MarketRepository,
  marketData: MarketData,
  marketApi: MarketApi,
  marketQueryHandler: MarketQueryHandler,
  marketCommandHandler: MarketCommandHandler,
  marketMeter: MarketMeter,
  marketTracing: MarketTracing
) extends AppEnv
