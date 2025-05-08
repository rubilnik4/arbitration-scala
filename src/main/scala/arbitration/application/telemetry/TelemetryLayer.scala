package arbitration.application.telemetry

import arbitration.application.configurations.AppConfig
import arbitration.application.telemetry.TelemetryResources.*
import io.opentelemetry.api
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.trace.SdkTracerProvider
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TelemetryLayer {
  private val otelSdkLive: ZLayer[AppConfig, Throwable, OpenTelemetrySdk] =
    (TracingLayer.tracingLive ++ MetricsLayer.metricsLive ++ LoggingLayer.loggingLive) >>>
      ZLayer.scoped {
        ZIO.fromAutoCloseable(
          for {
            tracing <- ZIO.service[SdkTracerProvider]
            metrics <- ZIO.service[SdkMeterProvider]
            logging <- ZIO.service[SdkLoggerProvider]
            sdk <- ZIO.succeed(
              OpenTelemetrySdk.builder()
                .setTracerProvider(tracing)
                .setMeterProvider(metrics)
                .setLoggerProvider(logging)
                .build()
            )
          } yield sdk
        )
      }

  private val tracingLayer: URLayer[OtelSdk & ContextStorage, Tracing] =
    OpenTelemetry.tracing(telemetryAppName)

  private val meteringLayer: URLayer[OtelSdk & ContextStorage, Meter] =
    OpenTelemetry.metrics(telemetryAppName)

  private def loggingLayer: ZLayer[OtelSdk & ContextStorage & AppConfig, Throwable, Unit] =
    ZLayer.scoped {
      for {       
        telemetryConfig <- getTelemetryConfig
        logLevel <- ZIO.fromOption(LogLevelMapper.parseLogLevel(telemetryConfig.logLevel))
          .orElseFail(new RuntimeException(s"Invalid log level: ${telemetryConfig.logLevel}"))

        _ <- OpenTelemetry.logging(telemetryAppName, logLevel).build
      } yield ()
    }

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[AppConfig, Throwable, Meter & Tracing] =
    (otelSdkLive ++ contextLayer ++ ZLayer.environment[AppConfig]) >>>
      (meteringLayer ++ loggingLayer ++ tracingLayer)
}
