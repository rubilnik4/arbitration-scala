package arbitration.application.environments

import arbitration.application.configurations.AppConfig
import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider
import zio.schema.codec.DecodeError.ReadError

object AppConfigLayer {
  val appConfigLayerLive: ZLayer[Any, Config.Error, AppConfig] = {
    val provider = TypesafeConfigProvider.fromResourcePath()
    ZLayer.fromZIO(
      read(AppConfig.config from provider)
    )
  }
}
