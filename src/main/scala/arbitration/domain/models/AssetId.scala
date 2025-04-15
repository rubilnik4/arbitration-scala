package arbitration.domain.models

final case class AssetId(id: String) extends AnyVal{
  override def toString: String = id
}

final case class AssetSpreadId(assetA: AssetId, assetB: AssetId) {
  override def toString: String = s"${assetA.id}-${assetB.id}"
}

object AssetSpreadId {
  def normalize(a: AssetId, b: AssetId): AssetSpreadId =
    if (a.id < b.id) AssetSpreadId(a, b)
    else AssetSpreadId(b, a)

  def toKey(assetSpread: AssetSpreadId): String =
    s"${assetSpread.assetA.id}|${assetSpread.assetB.id}"
}