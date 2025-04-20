package arbitration.infrastructure.caches

import arbitration.application.AppEnv
import zio.cache.{Cache, Lookup}
import zio.{ZIO, ZLayer}

object SpreadCacheLayer {
  val live: ZLayer[AppEnv, Nothing, SpreadCache] =
    ZLayer.fromZIO {
      for {
        repository <- ZIO.serviceWith[AppEnv](_.marketRepository)
        expiration <- ZIO.serviceWith[AppEnv](_.config.cache.spreadExpiration)

        cache <- Cache.make(
          capacity = 1000,
          timeToLive = expiration,
          lookup = Lookup(repository.getLastSpread)
        )

        memoryCache = new MemoryCacheLive(cache)
      } yield SpreadCacheLive(memoryCache)
    }
}
