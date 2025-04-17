package arbitration.application

import arbitration.application.configurations.AppConfig
import arbitration.application.queries.MarketData
import arbitration.domain.MarketError
import arbitration.infrastructure.caches.{PriceCache, SpreadCache}
import arbitration.infrastructure.repositories.{MarketRepository, QuillDatabaseContext}
import zio.logging.*

import javax.sql.DataSource

trait AppEnv {

//  def logger: Logger
  def config: AppConfig
  def priceCache: PriceCache
  def spreadCache: SpreadCache
  def marketRepository: MarketRepository
  def marketData: MarketData

  def quillDatabaseContext: QuillDatabaseContext
  def dataSource: DataSource
//  def cache: SpreadCache
//  def priceProvider: PriceProvider
//  def state: Ref[SpreadState]
}
