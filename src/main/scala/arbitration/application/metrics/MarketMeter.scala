package arbitration.application.metrics

import zio.ZIO

trait MarketMeter {
  def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A]
}
