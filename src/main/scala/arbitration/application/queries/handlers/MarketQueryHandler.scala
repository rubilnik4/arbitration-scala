package arbitration.application.queries.handlers

import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}

trait MarketQueryHandler {
  def priceQueryHandler: PriceQueryHandler
  def spreadQueryHandler: SpreadQueryHandler
}