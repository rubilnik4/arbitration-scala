package arbitration.application.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

trait MarketTracing {
  def tracing: Tracing
}
