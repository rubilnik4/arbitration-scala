package arbitration.application.queries.handlers

import zio.{ULayer, URLayer, ZLayer}

object MarketQueryHandlerLayer {
  private val priceQueryHandlerLive: ULayer[PriceQueryHandler] =
    ZLayer.succeed(new PriceQueryHandlerLive)

  private val spreadQueryHandlerLive: ULayer[SpreadQueryHandler] =
    ZLayer.succeed(new SpreadQueryHandlerLive)

  val marketQueryHandlerLive: ULayer[MarketQueryHandler] =
    (priceQueryHandlerLive ++ spreadQueryHandlerLive) >>> ZLayer.fromFunction {
    (price: PriceQueryHandler, spread: SpreadQueryHandler) =>
      MarketQueryHandlerLive(price, spread)
  }
}
