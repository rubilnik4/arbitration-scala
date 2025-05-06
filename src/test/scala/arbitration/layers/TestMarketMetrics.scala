package arbitration.layers

import arbitration.application.metrics.MarketMetrics
import zio.{UIO, ZIO, ZLayer}

object TestMarketMetrics {
  private val testMarketMetrics: MarketMetrics = new MarketMetrics {
    override def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio
  }

  val testMarketMetricsLive: ZLayer[Any, Nothing, MarketMetrics] =
    ZLayer.succeed(testMarketMetrics)
}
