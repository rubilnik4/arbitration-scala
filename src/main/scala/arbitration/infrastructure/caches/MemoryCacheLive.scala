package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.MarketError.NotFound
import zio.cache.Cache
import zio.{Cause, ZIO}

final class MemoryCacheLive[K, V](cache: Cache[K, MarketError, V]) extends MemoryCache[K, V] {
  override def getOrFetch(key: K): ZIO[Any, MarketError, V] =
    cache
      .get(key)
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get cache for key $key", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully got cache for key $key")
      )

  override def invalidate(key: K): ZIO[Any, MarketError, Unit] =
    cache.invalidate(key)
      .zipLeft(ZIO.logDebug(s"Successfully removed cache for key $key"))
}
