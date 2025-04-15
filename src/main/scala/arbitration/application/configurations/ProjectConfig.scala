package arbitration.application.configurations

import java.time.Duration

final case class AssetConfig (AssetA: String, AssetB: String)

final case class ProjectConfig(maxHistorySize: Int, spreadThreshold: BigDecimal, AssetLoadingDelay: Duration,
                               assets: AssetConfig)