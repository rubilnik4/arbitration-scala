package arbitration.domain.models

final case class AssetId(id: String) extends AnyVal {
  override def toString: String = id
}