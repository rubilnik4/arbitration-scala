package arbitration.layers

import arbitration.application.commands.handlers.MarketCommandHandlerLayer
import arbitration.application.environments.{AppEnv, AppEnvLayer}
import arbitration.application.queries.handlers.MarketQueryHandlerLayer
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
      MarketQueryHandlerLayer.marketQueryHandlerLive,
      MarketCommandHandlerLayer.marketCommandHandlerLive,
      TestMarketMetrics.testMarketMetricsLive,
      AppEnvLayer.appEnvLive
    )
}
