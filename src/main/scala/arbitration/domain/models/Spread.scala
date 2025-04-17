package arbitration.domain.models

import java.time.Instant
import java.util.UUID

final case class SpreadId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}

final case class Spread(priceA: Price, priceB: Price, value: BigDecimal, time: Instant)

final case class SpreadState(lastSpread: Option[BigDecimal], spreadHistory: List[BigDecimal], 
                             isThresholdExceeded: Boolean)

final case class SpreadResult(spread: Spread, spreadState: SpreadState)

object Spread {
  def toAssetSpread(spread: Spread): AssetSpreadId =
    AssetSpreadId(spread.priceA.asset, spread.priceB.asset)
    
  def toKey(spread: Spread): String =
    val assetSpread = toAssetSpread(spread)
    AssetSpreadId.toKey(assetSpread)
}