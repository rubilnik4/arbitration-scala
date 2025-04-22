package arbitration.application.queries.marketData

import arbitration.application.env.AppEnv
import arbitration.application.queries.marketData.MarketData
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import zio.ZIO

final class MarketDataLive extends MarketData {
  override def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] =
    for {
      api <- ZIO.serviceWith[AppEnv](_.marketApi)

      price <- api.getPrice(assetId)
    } yield price

  override def getLastPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] =
    for {
      cache <- ZIO.serviceWith[AppEnv](_.marketCache.priceCache)

      price <- cache.getOrFetch(assetId)
    } yield price

  override def getLastSpread(
      assetSpreadId: AssetSpreadId
  ): ZIO[AppEnv, MarketError, Spread] =
    for {
      cache <- ZIO.serviceWith[AppEnv](_.marketCache.spreadCache)

      spread <- cache.getOrFetch(assetSpreadId)
    } yield spread
}
