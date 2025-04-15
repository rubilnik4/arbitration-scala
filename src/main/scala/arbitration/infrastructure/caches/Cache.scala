package arbitration.infrastructure.caches

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import zio.ZIO

import java.time.Duration

trait Cache[K, V] {
  def get(key: K): ZIO[Any, MarketError, V]
  def set(key: K, value: V, expiration: Duration): ZIO[Any, MarketError, Unit]
  def remove(key: K): ZIO[Any, MarketError, Unit]
}

trait PriceCache extends Cache[AssetId, Price]

trait SpreadCache extends Cache[AssetSpreadId, Spread]