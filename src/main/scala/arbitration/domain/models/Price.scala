package arbitration.domain.models

import java.time.Instant

final case class Price(assetId: AssetId, value: BigDecimal, time: Instant)
