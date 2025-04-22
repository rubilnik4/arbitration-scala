package arbitration.application.commands.handlers

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import zio.ZIO

trait CommandHandler[-Cmd, +Res] {
  def execute(command: Cmd): ZIO[AppEnv, MarketError, Res]
}
