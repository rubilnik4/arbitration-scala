package arbitration.domain.models

import java.time.Instant

final case class Spread(priceA: Price, priceB: Price, value: BigDecimal, time: Instant)
