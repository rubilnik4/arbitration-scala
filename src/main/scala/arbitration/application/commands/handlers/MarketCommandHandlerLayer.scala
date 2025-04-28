package arbitration.application.commands.handlers

import zio.{ULayer, URLayer, ZLayer}

object MarketCommandHandlerLayer {
  private val spreadCommandHandlerLive: ULayer[SpreadCommandHandler] =
    ZLayer.succeed(new SpreadCommandHandlerLive)

  val marketCommandHandlerLive: ULayer[MarketCommandHandlerLive] =
    (spreadCommandHandlerLive) >>> ZLayer.fromFunction {
    (spread: SpreadCommandHandler) =>
      MarketCommandHandlerLive(spread)
  }
}
