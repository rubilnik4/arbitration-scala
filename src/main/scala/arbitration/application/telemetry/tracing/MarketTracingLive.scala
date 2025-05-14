package arbitration.application.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

final case class MarketTracingLive(tracing: Tracing) 
  extends MarketTracing 