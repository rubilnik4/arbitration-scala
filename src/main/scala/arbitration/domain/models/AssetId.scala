package arbitration.domain.models

final case class AssetId(id: String) extends AnyVal {
  override def toString: String = id
}

final case class AssetSpreadId(assetA: AssetId, assetB: AssetId) {
  override def toString: String = s"${assetA.id}-${assetB.id}"
}

object AssetSpreadId {
  def normalize(assetSpreadId: AssetSpreadId): AssetSpreadId =
    if (assetSpreadId.assetA.id < assetSpreadId.assetB.id)
      assetSpreadId
    else
      AssetSpreadId(assetSpreadId.assetB, assetSpreadId.assetA)

  def toKey(assetSpread: AssetSpreadId): String =
    s"${assetSpread.assetA.id}|${assetSpread.assetB.id}"
}
