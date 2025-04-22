package arbitration.environment

import arbitration.application.env.AppEnv
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    TestAppConfigLayer.testAppConfigLive >+>
      TestMarketRepositoryLayer.postgresMarketRepositoryLive >+>
      MarketCacheLayer.marketCacheLive >+>
      MarketDataLayer.marketDataLive >+>
      BinanceMarketApiLayer.binanceMarketApiLive >+>
      AppEnvLayer.appEnvLive
}
