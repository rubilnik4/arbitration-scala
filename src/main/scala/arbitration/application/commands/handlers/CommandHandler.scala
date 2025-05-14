package arbitration.application.commands.handlers

import arbitration.domain.MarketError
import arbitration.layers.AppEnv
import zio.ZIO

trait CommandHandler[-Cmd, +Res] {
  def handle(command: Cmd): ZIO[AppEnv, MarketError, Res]
}
