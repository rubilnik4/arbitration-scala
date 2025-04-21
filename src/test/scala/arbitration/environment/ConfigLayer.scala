package arbitration.environment

import arbitration.application.configurations.{AppConfig, AssetConfig, CacheConfig, PostgresConfig, ProjectConfig}

import scala.concurrent.duration.DurationInt

object ConfigLayer {
  private def assetConfig: AssetConfig =
    AssetConfig(
      assetA = "BTCUSDT_250627",
      assetB = "BTCUSDT_250926"
    )

  private def projectConfig: ProjectConfig =
    ProjectConfig(
      assets = assetConfig,
      spreadThreshold = BigDecimal(100000),
      maxHistorySize = 5,
      assetLoadingDelay = 15.seconds
    )

  private def cacheConfig: CacheConfig =
    CacheConfig(
      priceExpiration = 1.minute,
      spreadExpiration = 1.minute
    )

  def appConfig: AppConfig =
    AppConfig(
      project = projectConfig,
     // postgres = PostgresConfig(connectionString),
      cache = cacheConfig
    )
}
