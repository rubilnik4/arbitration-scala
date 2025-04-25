package arbitration.application.environments

import arbitration.application.configurations.AppConfig
import arbitration.application.queries.handlers.MarketQueryHandler
import arbitration.application.queries.marketData.MarketData
import arbitration.infrastructure.caches.MarketCache
import arbitration.infrastructure.markets.MarketApi
import arbitration.infrastructure.repositories.MarketRepository

final case class AppEnvLive (
  appConfig: AppConfig,
  marketCache: MarketCache,
  marketRepository: MarketRepository,
  marketData: MarketData,
  marketApi: MarketApi,
  marketQueryHandler: MarketQueryHandler
) extends AppEnv
