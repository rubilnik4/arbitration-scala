package arbitration.environment

import arbitration.application.{AppEnv, AppEnvLayer}
import arbitration.application.queries.marketData.MarketDataLayer
import arbitration.infrastructure.caches.MarketCacheLayer
import arbitration.infrastructure.markets.BinanceMarketApiLayer
import arbitration.infrastructure.repositories.MarketRepository
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestMarketRepositoryLayer.postgresMarketRepositoryLive,
      MarketCacheLayer.marketCacheLive,
      MarketDataLayer.marketDataLive,
      BinanceMarketApiLayer.binanceMarketApiLive,
      AppEnvLayer.appEnvLive
    )
}
