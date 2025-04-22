package arbitration.infrastructure.caches

import arbitration.application.configurations.AppConfig
import arbitration.infrastructure.repositories.MarketRepository
import zio.cache.{Cache, Lookup}
import zio.{ZIO, ZLayer}

import scala.jdk.DurationConverters.*

object MarketCacheLayer {
  private val priceCacheLayer: ZLayer[MarketRepository with AppConfig, Nothing, PriceCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.service[MarketRepository]
        expiration <- ZIO.serviceWith[AppConfig](_.cache.priceExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration.toJava,
          lookup = Lookup(repository.getLastPrice)
        )
        memoryCache = new MemoryCacheLive(cache)
      } yield PriceCacheLive(memoryCache)
    }

  private val spreadCacheLayer: ZLayer[MarketRepository with AppConfig, Nothing, SpreadCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.service[MarketRepository]
        expiration <- ZIO.serviceWith[AppConfig](_.cache.spreadExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration.toJava,
          lookup = Lookup(repository.getLastSpread)
        )

        memoryCache = new MemoryCacheLive(cache)
      } yield SpreadCacheLive(memoryCache)
    }

  val marketCacheLive: ZLayer[MarketRepository with AppConfig, Throwable, MarketCache] =
    (priceCacheLayer ++ spreadCacheLayer) >>> ZLayer.fromFunction {
      (price: PriceCache, spread: SpreadCache) =>
        MarketCacheLive(price, spread)
    }
}
