package arbitration.application.commands.handlers

import arbitration.application.environments.AppEnv
import arbitration.domain.MarketError
import zio.ZIO

trait CommandHandler[-Cmd, +Res] {
  def handle(command: Cmd): ZIO[AppEnv, MarketError, Res]
}
