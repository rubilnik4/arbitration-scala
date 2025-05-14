package arbitration.application.telemetry.tracing

import io.opentelemetry.context.propagation.TextMapPropagator
import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object MarketTracingLayer {
  val marketTracingLive: ZLayer[Tracing, Throwable, MarketTracing] =
    ZLayer.fromFunction { (tracing: Tracing) =>
      MarketTracingLive(tracing)
    }
}
