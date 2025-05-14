package arbitration.application.queries.handlers

import arbitration.application.queries.queries.SpreadQuery
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetSpreadId, Spread}
import arbitration.layers.AppEnv
import zio.ZIO

final class SpreadQueryHandlerLive extends SpreadQueryHandler {
  override def handle(input: SpreadQuery): ZIO[AppEnv, MarketError, Spread] = {
    val assetSpreadId = AssetSpreadId.normalize(input.assetSpreadId)

    for {
      _ <- ZIO.logInfo(s"Execute spread query for assets: $assetSpreadId")
      marketData <- ZIO.serviceWith[AppEnv](_.marketData)
      spread <- marketData.getLastSpread(assetSpreadId)
    } yield spread
  }
}
