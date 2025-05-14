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

trait AppEnv {
  def appConfig: AppConfig
  def marketCache: MarketCache
  def marketRepository: MarketRepository
  def marketData: MarketData
  def marketApi: MarketApi
  def marketQueryHandler: MarketQueryHandler
  def marketCommandHandler: MarketCommandHandler
  def marketMeter: MarketMeter
  def marketTracing: MarketTracing
}
