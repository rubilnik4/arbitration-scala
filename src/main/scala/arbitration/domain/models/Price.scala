package arbitration.domain

import java.time.Instant

final case class Price(asset: AssetId, value: BigDecimal, time: Instant)
