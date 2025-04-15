package arbitration.application

import arbitration.infrastructure.MarketRepository
import zio.logging._

trait Environment {
  def marketRepository: MarketRepository
//  def logger: Logger
//  def config: ProjectConfig
//  def cache: SpreadCache
//  def priceProvider: PriceProvider
//  def state: Ref[SpreadState]
}
