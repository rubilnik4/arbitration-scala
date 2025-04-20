package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.MarketError.{CacheError, NotFound}
import zio.cache.Cache
import zio.{Cause, ZIO}

final class MemoryCacheLive[K, V](cache: Cache[K, MarketError, V]) extends MemoryCache[K, V] {
  override def getOrFetch(key: K): ZIO[Any, MarketError, V] = 
    ZIO.logDebug(s"Getting cache for key $key")

    cache.get(key)
      .tapError(e => ZIO.logErrorCause(s"Failed to get cache for key $key", Cause.fail(e)))
      .mapError {
        case _: NoSuchElementException => NotFound(s"Cache for key $key not found")      
        case other => other
      }

  override def invalidate(key: K): ZIO[Any, MarketError, Unit] =    
    ZIO.logDebug(s"Removing cache for key $key")

    cache.invalidate(key)
}
