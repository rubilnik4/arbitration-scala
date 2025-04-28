package arbitration.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class AssetConfig(assetA: String, assetB: String)

object AssetConfig {
  implicit val config: Config[AssetConfig] = deriveConfig[AssetConfig]
}