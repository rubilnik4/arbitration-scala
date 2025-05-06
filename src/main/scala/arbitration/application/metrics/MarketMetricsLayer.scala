package arbitration.application.metrics

import zio.telemetry.opentelemetry.metrics.Meter
import zio.{Clock, ZIO, ZLayer}

object MarketMetricsLayer {
  val MarketMetricsLive: ZLayer[Meter, Nothing, MarketMetrics] =
    ZLayer.fromZIO {
      for {
        meter <- ZIO.service[Meter]
        histogram <- meter.histogram(
          name = "spread_duration",
          description = Some("Duration of spread computation in milliseconds"),
          unit = Some("ms")
        )
      } yield new MarketMetrics {
        def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] =
          for {
            start <- Clock.nanoTime
            result <- zio
            end <- Clock.nanoTime
            durationMillis = (end - start) / 1_000_000
            _ <- histogram.record(durationMillis.toDouble)
          } yield result
      }
    }
}
