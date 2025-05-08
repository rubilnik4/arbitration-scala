package arbitration.layers

import arbitration.application.telemetry.TelemetryResources.*
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object TestMarketTracingLayer {
  private val tracingLive: ZLayer[Any, Throwable, SdkTracerProvider] = ZLayer.scoped {
    for {
      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(OtlpJsonLoggingSpanExporter.create())
      )

      processor <- ZIO.fromAutoCloseable(
        ZIO.succeed(SimpleSpanProcessor.create(exporter))
      )

      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkTracerProvider.builder()
            .setResource(telemetryResource)
            .addSpanProcessor(processor)
            .build()
        )
      )
    } yield provider
  }

  private val otelSdkLive: ZLayer[Any, Throwable, OpenTelemetrySdk] =
    tracingLive >>>
      ZLayer.scoped {
        ZIO.fromAutoCloseable(
          for {
            tracing <- ZIO.service[SdkTracerProvider]
            sdk <- ZIO.succeed(
              OpenTelemetrySdk.builder()
                .setTracerProvider(tracing)
                .build()
            )
          } yield sdk
        )
      }

  private val tracingLayer: URLayer[OtelSdk & ContextStorage, Tracing] =
    OpenTelemetry.tracing(telemetryAppName)

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[Any, Throwable, Tracing] =
    (otelSdkLive ++ contextLayer) >>> tracingLayer
}

