package arbitration.infrastructure.repositories

import arbitration.domain.MarketError
import arbitration.domain.models.*
import zio.ZIO

trait MarketRepository {
  def saveSpread(spread: Spread): ZIO[Any, MarketError, SpreadId]
  def getLastPrice(assetId: AssetId): ZIO[Any, MarketError, Price]
  def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[Any, MarketError, Spread]
}
