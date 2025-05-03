package arbitration.application.logging

import io.opentelemetry.api
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.`export`.*
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object LoggingLayer {
  private val resource = Resource.create(
    Attributes.of(ServiceAttributes.SERVICE_NAME, "arbitration-app"))

  private val otelSdkLayer: TaskLayer[OtelSdk] = ZLayer.scoped {
    for {
      spanExporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .build()))

      spanProcessor <- ZIO.fromAutoCloseable(
        ZIO.succeed(SimpleSpanProcessor.create(spanExporter)))

      tracerProvider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(spanProcessor)
            .build()))

      logExporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcLogRecordExporter.builder()
            .setEndpoint("http://localhost:4317")
            .build()))

      logRecordProcessor <- ZIO.fromAutoCloseable(
        ZIO.succeed(SimpleLogRecordProcessor.create(logExporter)))

      loggerProvider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(logRecordProcessor)
            .build()
        )
      )

      sdk <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(loggerProvider)
            .build()
        )
      )
    } yield sdk
  }
  

  private val tracingLayer: URLayer[OtelSdk & ContextStorage, Tracing] =
    OpenTelemetry.tracing("arbitration-app")

  private val loggingLayer: URLayer[OtelSdk & ContextStorage, Unit] =
    OpenTelemetry.logging("arbitration-app", LogLevel.Debug)

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[Any, Throwable, Unit & Tracing] =
    (otelSdkLayer ++ contextLayer) >>>
      (loggingLayer ++ tracingLayer)
}
