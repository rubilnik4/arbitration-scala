package arbitration.domain.models

import java.util.UUID

final case class SpreadId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
