package arbitration.infrastructure.caches

import arbitration.application.configurations.CacheConfig
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetSpreadId, Spread}
import zio.ZIO
import java.time.Duration

trait SpreadCache extends Cache[AssetSpreadId, Spread]