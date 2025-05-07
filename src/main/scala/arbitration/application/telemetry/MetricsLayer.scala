package arbitration.application.telemetry

import arbitration.application.telemetry.TelemetryResources.telemetryResource
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import zio.*

object MetricsLayer {
  val metricsLive: ZLayer[Any, Throwable, SdkMeterProvider] = ZLayer.scoped {
    for {     
      prometheusServer <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          PrometheusHttpServer.builder()
            .setPort(9464)
            .build()
        )
      )
      
      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkMeterProvider.builder()
            .registerMetricReader(prometheusServer)
            .setResource(telemetryResource)
            .build()
        )
      )
    } yield provider
  }
}
