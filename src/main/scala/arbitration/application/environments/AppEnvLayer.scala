package arbitration.application.environments

import arbitration.application.commands.handlers.MarketCommandHandler
import arbitration.application.configurations.AppConfig
import arbitration.application.queries.handlers.MarketQueryHandler
import arbitration.application.queries.marketData.MarketData
import arbitration.infrastructure.caches.MarketCache
import arbitration.infrastructure.markets.MarketApi
import arbitration.infrastructure.repositories.MarketRepository
import zio.ZLayer

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      with MarketCache with MarketRepository with MarketData
      with MarketApi with MarketQueryHandler with MarketCommandHandler,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
