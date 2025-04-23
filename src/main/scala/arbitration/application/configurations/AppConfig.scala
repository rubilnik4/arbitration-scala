package arbitration.application.configurations

final case class AppConfig(
  project: ProjectConfig,
  cache: CacheConfig,
  postgres: Option[PostgresConfig]
)
