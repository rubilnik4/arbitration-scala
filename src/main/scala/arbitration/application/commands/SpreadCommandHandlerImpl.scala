package arbitration.application.commands

import arbitration.application.Environment
import arbitration.application.commands.SpreadCommandHandler
import arbitration.application.queries.MarketData
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import zio.ZIO

class SpreadCommandHandlerImpl extends SpreadCommandHandler {
    private def getPrice(assetId: AssetId): ZIO[MarketData with Logging, MarketError, Price] =
      for {
        data <- ZIO.service[MarketData]
        logger <- ZIO.service[Logging]
        result <- data.getPrice(assetId)
          .catchAll { _ =>
            data.getLastPrice(assetId)
              .tapBoth(
                _ => logger.error(s"No price available at all ${assetId.value}"),
                _ => logger.info(s"Using last price for ${assetId.value}")
              )
          }
      } yield result
    
    override def execute(cmd: SpreadCommand): ZIO[Environment, MarketError, Spread] = {
      val normalizedIds = AssetSpreadId.normalize(cmd.assetA, cmd.assetB)

      for {
        env     <- ZIO.environment[Environment]
        logger  <- ZIO.service[Logging]
        _       <- logger.info(s"Executing spread command for ${normalizedIds._1.value}-${normalizedIds._2.value}")

        priceA  <- getPrice(normalizedIds._1)
        priceB  <- getPrice(normalizedIds._2)

        spread   = getSpread(priceA, priceB)
        _       <- saveSpread(spread)

        config  <- ZIO.service[ConfigService]
        _       <- ZIO.succeed(updateState(env.getState, spread, config.spreadThreshold))
      } yield spread
    }
}
