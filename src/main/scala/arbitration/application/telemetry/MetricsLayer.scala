package arbitration.application.telemetry

import arbitration.application.telemetry.TelemetryResources.telemetryResource
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.PeriodicMetricReader
import zio.*

object MetricsLayer {
  val metricsLive: ZLayer[Any, Throwable, SdkMeterProvider] = ZLayer.scoped {
    for {
      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(OtlpJsonLoggingMetricExporter.create())
      )

      reader <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          PeriodicMetricReader.builder(exporter)
            .setInterval(5.seconds)
            .build()
        )
      )

      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .setResource(telemetryResource)
            .build()
        )
      )
    } yield provider
  }
}
