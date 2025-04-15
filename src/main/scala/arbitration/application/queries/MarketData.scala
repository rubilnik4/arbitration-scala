package arbitration.application.queries

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO

trait MarketData {
  def getPrice(assetId: AssetId): ZIO[Any, MarketError, Price]
  def getLastPrice(assetId: AssetId): ZIO[Any, MarketError, Price]
}
