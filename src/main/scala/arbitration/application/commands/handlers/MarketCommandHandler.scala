package arbitration.application.commands.handlers

import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}

trait MarketCommandHandler {
  def spreadCommandHandler: SpreadCommandHandler
}