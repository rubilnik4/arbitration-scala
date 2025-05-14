package arbitration.layers

import arbitration.application.configurations.AppConfig
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object AppConfigLayer {
  val appConfigLive: ZLayer[Any, Config.Error, AppConfig] = {
    val provider = TypesafeConfigProvider.fromResourcePath()
    ZLayer.fromZIO(
      read(AppConfig.config from provider)
    )
  }
}
