package arbitration.application.commands

import arbitration.application.Environment
import arbitration.domain.MarketError
import arbitration.domain.models.Spread
import zio.ZIO

trait SpreadCommandHandler {
  def execute(cmd: SpreadCommand): ZIO[Environment, MarketError, Spread]
}
