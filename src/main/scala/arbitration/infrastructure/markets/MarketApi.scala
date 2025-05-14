package arbitration.infrastructure.markets

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import arbitration.layers.AppEnv
import zio.ZIO

trait MarketApi {
  def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price]
}
