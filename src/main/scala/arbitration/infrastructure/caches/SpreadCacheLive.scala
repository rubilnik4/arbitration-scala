package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import zio.ZIO

final case class SpreadCacheLive(cache: MemoryCache[AssetSpreadId, Spread])
    extends SpreadCache {
  override def getOrFetch(
      assetSpreadId: AssetSpreadId
  ): ZIO[Any, MarketError, Spread] = cache.getOrFetch(assetSpreadId)

  override def invalidate(
      assetSpreadId: AssetSpreadId
  ): ZIO[Any, MarketError, Unit] = cache.invalidate(assetSpreadId)
}
