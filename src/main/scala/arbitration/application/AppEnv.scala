package arbitration.application

import arbitration.application.configurations.AppConfig
import arbitration.application.queries.MarketData
import arbitration.infrastructure.caches.{MarketCache, PriceCache, SpreadCache}
import arbitration.infrastructure.markets.MarketApi
import arbitration.infrastructure.repositories.MarketRepository

trait AppEnv {
  def config: AppConfig
  def marketCache: MarketCache
  def marketRepository: MarketRepository
  def marketData: MarketData
  def marketApi: MarketApi
}
