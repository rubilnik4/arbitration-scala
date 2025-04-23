package arbitration.infrastructure.markets

import arbitration.application.environments.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO

trait MarketApi {
  def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price]
}
