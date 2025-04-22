package arbitration.application.queries.handlers

import arbitration.application.env.AppEnv
import arbitration.application.queries.queries.PriceQuery
import arbitration.domain.MarketError
import arbitration.domain.models.Price
import zio.ZIO

final class PriceQueryHandlerLive extends PriceQueryHandler {
  override def handle(input: PriceQuery): ZIO[AppEnv, MarketError, Price] = {
    val assetId = input.assetId
    for {
      _ <- ZIO.logInfo(s"Execute price query for assets: $assetId")
      marketData <- ZIO.serviceWith[AppEnv](_.marketData)
      price <- marketData.getLastPrice(assetId)
    } yield price
  }
}
