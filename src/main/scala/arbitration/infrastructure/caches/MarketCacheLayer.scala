package arbitration.infrastructure.caches

import arbitration.application.env.AppEnv
import zio.cache.{Cache, Lookup}
import zio.{ZIO, ZLayer}

import scala.jdk.DurationConverters.*

object MarketCacheLayer {
  private val priceCacheLayer: ZLayer[AppEnv, Nothing, PriceCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.serviceWith[AppEnv](_.marketRepository)
        expiration <- ZIO.serviceWith[AppEnv](_.config.cache.priceExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration.toJava,
          lookup = Lookup(repository.getLastPrice)
        )
        memoryCache = new MemoryCacheLive(cache)
      } yield PriceCacheLive(memoryCache)
    }

  private val spreadCacheLayer: ZLayer[AppEnv, Nothing, SpreadCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.serviceWith[AppEnv](_.marketRepository)
        expiration <- ZIO.serviceWith[AppEnv](_.config.cache.spreadExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration.toJava,
          lookup = Lookup(repository.getLastSpread)
        )

        memoryCache = new MemoryCacheLive(cache)
      } yield SpreadCacheLive(memoryCache)
    }

  val marketCacheLive: ZLayer[AppEnv, Throwable, MarketCache] =
    (priceCacheLayer ++ spreadCacheLayer) >>> ZLayer.fromFunction {
      (price: PriceCache, spread: SpreadCache) =>
        MarketCacheLive(price, spread)
    }
}
