package arbitration.infrastructure.caches

import arbitration.application.AppEnv
import arbitration.domain.models.{AssetId, Price}
import zio.cache.{Cache, Lookup}
import zio.{ZIO, ZLayer}

object PriceCacheLayer {
  val live: ZLayer[AppEnv, Nothing, PriceCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.serviceWith[AppEnv](_.marketRepository)
        expiration <- ZIO.serviceWith[AppEnv](_.config.cache.priceExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration,
          lookup = Lookup(repository.getLastPrice)
        )
        memoryCache = new MemoryCacheLive(cache)
      }  yield PriceCacheLive(memoryCache)
    }
}
