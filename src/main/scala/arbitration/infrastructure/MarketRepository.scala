package arbitration.infrastructure

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Spread}
import zio.ZIO

trait MarketRepository {
  def saveSpread(spread: Spread): ZIO[AppEnv, MarketError, AssetId]
}
