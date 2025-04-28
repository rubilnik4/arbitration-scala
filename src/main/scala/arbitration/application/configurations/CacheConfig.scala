package arbitration.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class CacheConfig(
  priceExpiration: Duration,
  spreadExpiration: Duration
)

object CacheConfig {
  implicit val config: Config[CacheConfig] = deriveConfig[CacheConfig]
}
