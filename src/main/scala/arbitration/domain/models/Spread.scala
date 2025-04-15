package arbitration.domain.models

import arbitration.domain.models.AssetSpreadId.toKey
import java.time.Instant

final case class Spread(priceA: Price, priceB: Price, value: BigDecimal, time: Instant)

final case class SpreadState(lastSpread: Option[BigDecimal], spreadHistory: List[BigDecimal], 
                             isThresholdExceeded: Boolean)

object Spread {
  def toAssetSpread(spread: Spread): AssetSpreadId =
    AssetSpreadId(spread.priceA.asset, spread.priceB.asset)
    
  def toKey(spread: Spread): String =
    val assetSpread = toAssetSpread(spread)
    AssetSpreadId.toKey(assetSpread)
}