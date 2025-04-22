package arbitration.application.queries.marketData

import zio.{ULayer, ZLayer}

object MarketDataLayer {
  val marketDataLive: ULayer[MarketData] =
    ZLayer.succeed(new MarketDataLive)
}
