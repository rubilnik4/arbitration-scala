package arbitration.application.configurations

import scala.concurrent.duration.Duration

final case class ProjectConfig(
    maxHistorySize: Int,
    spreadThreshold: BigDecimal,
    assetLoadingDelay: Duration,
    assets: AssetConfig
)
