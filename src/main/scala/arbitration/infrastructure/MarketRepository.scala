package arbitration.infrastructure

import arbitration.application.Environment
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Spread}
import zio.ZIO

trait MarketRepository {
  def saveSpread(spread: Spread): ZIO[Environment, MarketError, AssetId]
}
