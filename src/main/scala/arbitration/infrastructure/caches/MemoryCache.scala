package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import zio.ZIO

trait MemoryCache[K, V] {
  def getOrFetch(key: K): ZIO[Any, MarketError, V]
  def invalidate(key: K): ZIO[Any, MarketError, Unit]
}

trait PriceCache extends MemoryCache[AssetId, Price]

trait SpreadCache extends MemoryCache[AssetSpreadId, Spread]

trait MarketCache {
  def priceCache: PriceCache
  def spreadCache: SpreadCache
}
