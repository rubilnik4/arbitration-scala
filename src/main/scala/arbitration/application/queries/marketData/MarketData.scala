package arbitration.application.queries.marketData

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import arbitration.layers.AppEnv
import zio.ZIO

trait MarketData {
  def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price]
  def getLastPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price]
  def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[AppEnv, MarketError, Spread]
}
