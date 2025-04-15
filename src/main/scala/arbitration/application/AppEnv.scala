package arbitration.application

import arbitration.application.configurations.AppConfig
import arbitration.application.queries.MarketData
import arbitration.domain.MarketError
import arbitration.infrastructure.MarketRepository
import arbitration.infrastructure.caches.{PriceCache, SpreadCache}
import zio.logging.*

trait AppEnv {

//  def logger: Logger
  def config: AppConfig
  def priceCache: PriceCache
  def spreadCache: SpreadCache
  def marketRepository: MarketRepository
  def marketData: MarketData
//  def cache: SpreadCache
//  def priceProvider: PriceProvider
//  def state: Ref[SpreadState]
}
