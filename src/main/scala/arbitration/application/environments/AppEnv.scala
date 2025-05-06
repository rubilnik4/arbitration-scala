package arbitration.application.environments

import arbitration.application.commands.handlers.MarketCommandHandler
import arbitration.application.configurations.AppConfig
import arbitration.application.metrics.MarketMetrics
import arbitration.application.queries.handlers.MarketQueryHandler
import arbitration.application.queries.marketData.MarketData
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
  def marketMetrics: MarketMetrics
}
