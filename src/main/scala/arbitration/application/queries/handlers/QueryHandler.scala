package arbitration.application.queries.handlers

import arbitration.domain.MarketError
import arbitration.layers.AppEnv
import zio.ZIO

trait QueryHandler[-In, +Out] {
  def handle(input: In): ZIO[AppEnv, MarketError, Out]
}
