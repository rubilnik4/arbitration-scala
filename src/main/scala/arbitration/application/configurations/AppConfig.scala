package arbitration.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig
import zio.config.derivation._

final case class AppConfig(
  project: ProjectConfig,
  cache: CacheConfig,
  postgres: Option[PostgresConfig]
)

object AppConfig {
  implicit val config: Config[AppConfig] = deriveConfig[AppConfig]
}
