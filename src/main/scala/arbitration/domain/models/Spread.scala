package arbitration.domain.models

import java.time.Instant

final case class Spread(
    priceA: Price,
    priceB: Price,
    value: BigDecimal,
    time: Instant
)

object Spread {
  def toAssetSpread(spread: Spread): AssetSpreadId =
    AssetSpreadId(spread.priceA.asset, spread.priceB.asset)

  def toKey(spread: Spread): String =
    val assetSpread = toAssetSpread(spread)
    AssetSpreadId.toKey(assetSpread)
}