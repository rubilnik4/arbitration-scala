package arbitration.application.queries.handlers

import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}

final case class MarketQueryHandlerLive(
  priceQueryHandler: PriceQueryHandler, 
  spreadQueryHandler: SpreadQueryHandler
) extends MarketQueryHandler 
