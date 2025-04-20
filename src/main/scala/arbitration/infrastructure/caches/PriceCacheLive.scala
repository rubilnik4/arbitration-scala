package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO

final case class PriceCacheLive(cache: MemoryCache[AssetId, Price]) extends PriceCache {
  override def getOrFetch(assetId: AssetId): ZIO[Any, MarketError, Price] =
    cache.getOrFetch(assetId)

  override def invalidate(assetId: AssetId): ZIO[Any, MarketError, Unit] = 
    cache.invalidate(assetId)
}
