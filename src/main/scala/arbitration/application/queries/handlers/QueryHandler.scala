package arbitration.application.queries.handlers

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import zio.ZIO

trait QueryHandler[-In, +Out] {
  def handle(input: In): ZIO[AppEnv, MarketError, Out]
}
