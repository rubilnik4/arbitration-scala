package arbitration.application.commands.handlers

import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}

final case class MarketCommandHandlerLive(
  spreadCommandHandler: SpreadCommandHandler
) extends MarketCommandHandler 
