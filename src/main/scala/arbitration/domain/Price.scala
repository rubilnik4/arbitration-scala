package arbitration.domain

import java.time.Instant

final case class Price(asset: Asset, value: BigDecimal, time: Instant)
