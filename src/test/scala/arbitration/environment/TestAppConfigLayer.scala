package arbitration.environment

import arbitration.application.configurations.*
import zio.ZLayer

import scala.concurrent.duration.DurationInt

object TestAppConfigLayer {
  private lazy val appConfig: AppConfig =
    AppConfig(
      project = ProjectConfig(
        assets = AssetConfig(
          assetA = "BTCUSDT_250627",
          assetB = "BTCUSDT_250926"
        ),
        spreadThreshold = BigDecimal(100000),
        maxHistorySize = 5,
        assetLoadingDelay = 15.seconds
      ),
      postgres = PostgresConfig(""),
      cache = CacheConfig(
        priceExpiration = 1.minute,
        spreadExpiration = 1.minute
      )
    )

  val testAppConfigLive: ZLayer[Any, Nothing, AppConfig] =
    ZLayer.succeed(appConfig)
}
