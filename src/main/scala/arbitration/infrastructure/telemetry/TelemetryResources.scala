package arbitration.infrastructure.telemetry

import arbitration.application.configurations.{AppConfig, TelemetryConfig}
import arbitration.layers.AppEnv
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import zio.ZIO

object TelemetryResources {
  final val telemetryAppName = "arbitration-app"

  val telemetryResource: Resource = Resource.create(
    Attributes.of(ServiceAttributes.SERVICE_NAME, telemetryAppName))

  def getTelemetryConfig: ZIO[AppConfig, Throwable, TelemetryConfig] =
    for {
      config <- ZIO.service[AppConfig]
      telemetryConfig <- ZIO.fromOption(config.telemetry)
        .orElseFail(new RuntimeException("Telemetry config is missing"))
    } yield telemetryConfig
}
