package arbitration

import arbitration.api.routes.{ApiRoutesLayer, HttpServerLayer}
import arbitration.application.commands.handlers.MarketCommandHandlerLayer
import arbitration.application.environments.*
import arbitration.application.jobs.SpreadJobLayer
import arbitration.application.queries.handlers.MarketQueryHandlerLayer
import arbitration.application.queries.marketData.MarketDataLayer
import arbitration.infrastructure.caches.MarketCacheLayer
import arbitration.infrastructure.markets.BinanceMarketApiLayer
import arbitration.infrastructure.repositories.PostgresMarketRepositoryLayer
import zio.ZLayer

object MainAppLayer {
  private val appLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      AppConfigLayer.appConfigLive,
      PostgresMarketRepositoryLayer.postgresMarketRepositoryLive,
      MarketCacheLayer.marketCacheLive,
      MarketDataLayer.marketDataLive,
      BinanceMarketApiLayer.binanceMarketApiLive,
      MarketQueryHandlerLayer.marketQueryHandlerLive,
      MarketCommandHandlerLayer.marketCommandHandlerLive,
    
      AppEnvLayer.appEnvLive
    )

  val runtimeLive: ZLayer[Any, Throwable, Unit] =
    ZLayer.make[Unit](
      appLive,
      SpreadJobLayer.spreadJobLayer,
      ApiRoutesLayer.apiRoutesLive,
      HttpServerLayer.httpServerLive
    )
}