package arbitration.domain.models

final case class AssetId(id: String) extends AnyVal

final case class AssetSpreadId(assetA: AssetId, assetB: AssetId)

object AssetSpreadId {
  def normalize(a: AssetId, b: AssetId): AssetSpreadId =
    if (a.id < b.id) AssetSpreadId(a, b)
    else AssetSpreadId(b, a)

  def toKey(spread: AssetSpreadId): String =
    s"${spread.assetA.id}|${spread.assetB.id}"
}