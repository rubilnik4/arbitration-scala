package arbitration.infrastructure.repositories

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread, SpreadId}
import zio.{IO, ZIO}

import javax.sql.DataSource

trait MarketRepository {
  def saveSpread(spread: Spread):  ZIO[Any, MarketError, SpreadId]
  def getLastPrice(assetId: AssetId): ZIO[Any, MarketError, Price]
  def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[Any, MarketError, Spread]
}
