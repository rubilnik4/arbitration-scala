package arbitration.domain.models

import java.time.Instant

final case class Spread(
    priceA: Price,
    priceB: Price,
    value: BigDecimal,
    time: Instant
) {
  override def toString: String = s"Spread:${priceA.assetId}-${priceB.assetId}, ${value}"
}

object Spread {
  def toAssetSpread(spread: Spread): AssetSpreadId =
    AssetSpreadId(spread.priceA.assetId, spread.priceB.assetId)

  def toKey(spread: Spread): String =
    val assetSpread = toAssetSpread(spread)
    AssetSpreadId.toKey(assetSpread)
}