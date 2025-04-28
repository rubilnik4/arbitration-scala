package arbitration.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig
import zio.Duration

final case class ProjectConfig(
    maxHistorySize: Int,
    spreadThreshold: BigDecimal,
    assetLoadingDelay: Duration,
    assets: AssetConfig
)

object ProjectConfig {
  implicit val config: Config[ProjectConfig] = deriveConfig[ProjectConfig]
}
