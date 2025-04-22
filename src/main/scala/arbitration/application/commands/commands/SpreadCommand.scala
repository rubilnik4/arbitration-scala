package arbitration.application.commands.commands

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Spread, SpreadState}
import zio.ZIO

final case class SpreadCommand(
    spreadState: SpreadState,
    assetSpreadId: AssetSpreadId
)
