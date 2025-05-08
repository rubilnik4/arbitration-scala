package arbitration.layers

import arbitration.application.metrics.MarketMeter
import zio.{UIO, ZIO, ZLayer}

object TestMarketMeterLayer {
  private val testMarketMeter: MarketMeter = new MarketMeter {
    override def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio
  }

  val testMarketMeterLive: ZLayer[Any, Nothing, MarketMeter] =
    ZLayer.succeed(testMarketMeter)
}
