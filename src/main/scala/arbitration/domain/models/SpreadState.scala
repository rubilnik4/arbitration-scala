package arbitration.domain.models

final case class SpreadState(
  lastSpread: Option[BigDecimal],
  spreadHistory: List[BigDecimal],
  isThresholdExceeded: Boolean
)

object SpreadState {
  def Init(): SpreadState =
    SpreadState(None, List.empty, false)
}
