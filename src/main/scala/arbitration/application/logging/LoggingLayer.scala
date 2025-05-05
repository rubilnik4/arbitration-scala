package arbitration.application.logging

import arbitration.application.configurations.*
import arbitration.application.environments.AppEnv
import io.opentelemetry.api
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.logs.`export`.*
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object LoggingLayer {
  private val appName = "arbitration-app"

  private val resource = Resource.create(
    Attributes.of(ServiceAttributes.SERVICE_NAME, appName))

  private def getTelemetryConfig(appConfig: AppConfig): TelemetryConfig =
    appConfig.telemetry.getOrElse {
      throw new RuntimeException("Telemetry config is missing")
    }

  private val otelSdkLayer: ZLayer[AppEnv, Throwable, OtelSdk] = ZLayer.scoped {
    for {
      appConfig <- ZIO.serviceWith[AppEnv](_.appConfig)
      telemetryConfig = getTelemetryConfig(appConfig)

      spanExporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcSpanExporter.builder()
            .setEndpoint(telemetryConfig.otelEndpoint)
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
            .setEndpoint(telemetryConfig.otelEndpoint)
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
    OpenTelemetry.tracing(appName)

  private def loggingLayer: ZLayer[OtelSdk & ContextStorage & AppEnv, Throwable, Unit] =
    OpenTelemetry.logging(appName, LogLevel.Debug)
//    ZLayer.scoped {
//      for {
//        env <- ZIO.service[AppEnv]
//        telemetryConfig = getTelemetryConfig(env.appConfig)
//        logLevel <- ZIO.fromOption(LogLevelMapper.parseLogLevel(telemetryConfig.logLevel))
//          .orElseFail(new RuntimeException(s"Invalid log level: ${telemetryConfig.logLevel}"))
//
//        _ <- OpenTelemetry.logging(appName, logLevel).build
//      } yield ()
//    }

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[AppEnv, Throwable, Unit & Tracing] =
    (otelSdkLayer ++ contextLayer ++ ZLayer.environment[AppEnv]) >>>
      (loggingLayer ++ tracingLayer)
}
