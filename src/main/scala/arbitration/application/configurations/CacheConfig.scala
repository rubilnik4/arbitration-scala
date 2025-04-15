package arbitration.application.configurations

import java.time.Duration

final case class CacheConfig(priceExpiration: Duration, spreadExpiration: Duration)
