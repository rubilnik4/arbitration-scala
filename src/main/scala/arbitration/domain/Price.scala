package arbitration.domain

import java.time.Instant

final case class Price(symbol: Asset, value: BigDecimal, timestamp: Instant)
