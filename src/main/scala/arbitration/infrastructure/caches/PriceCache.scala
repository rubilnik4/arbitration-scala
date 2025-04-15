package arbitration.infrastructure.caches

import arbitration.application.configurations.CacheConfig
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO
import java.time.Duration

trait PriceCache extends Cache[AssetId, Price]


