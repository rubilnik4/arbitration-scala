package arbitration.application.queries.handlers

import arbitration.application.queries.queries.PriceQuery
import arbitration.domain.models.Price

trait PriceQueryHandler extends QueryHandler[PriceQuery, Price]
