package arbitration.infrastructure.caches

final case class MarketCacheLive(
  priceCache: PriceCache,
  spreadCache: SpreadCache
) extends MarketCache
