package arbitration.application.queries

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO

class MarketDataImpl extends MarketData:
  override def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] = 

  override def getLastPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] = ???
