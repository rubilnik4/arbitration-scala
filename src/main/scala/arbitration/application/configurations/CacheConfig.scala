package arbitration.application.configurations

import scala.concurrent.duration.{Duration, FiniteDuration}

final case class CacheConfig(
  priceExpiration: FiniteDuration,
  spreadExpiration: FiniteDuration
)
