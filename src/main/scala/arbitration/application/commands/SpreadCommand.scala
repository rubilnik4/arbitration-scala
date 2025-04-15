package arbitration.application.commands

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Spread}
import zio.ZIO

final case class SpreadCommand(assetA: AssetId, assetB: AssetId)