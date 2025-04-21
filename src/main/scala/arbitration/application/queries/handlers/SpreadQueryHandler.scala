package arbitration.application.queries.handlers

import arbitration.application.AppEnv
import arbitration.application.queries.queries.SpreadQuery
import arbitration.domain.MarketError
import arbitration.domain.models.{Spread, SpreadResult}
import zio.ZIO

trait SpreadQueryHandler extends QueryHandler[SpreadQuery, Spread]
