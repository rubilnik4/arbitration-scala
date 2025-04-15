package arbitration.application.commands

import arbitration.application.Environment
import arbitration.application.commands.SpreadCommandHandler
import arbitration.application.queries.MarketData
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread}
import arbitration.infrastructure.MarketRepository
import arbitration.infrastructure.caches.{PriceCache, SpreadCache}
import zio.ZIO

import java.time.Instant

class SpreadCommandHandlerImpl extends SpreadCommandHandler {
  private def getPrice(assetId: AssetId): ZIO[MarketData, MarketError, Price] =
    for {
      marketData <- ZIO.service[MarketData]
      _ <- ZIO.logInfo(s"Fetching price for $assetId")
      result <- marketData.getPrice(assetId)
        .catchAll { _ =>
          marketData.getLastPrice(assetId)
            .tapError(_ => ZIO.logError(s"No price at all for $assetId"))
            .tap(_ => ZIO.logInfo(s"Using last price for $assetId"))
        }
    } yield result

  private def getSpread(priceA: Price, priceB: Price): Spread = {
    val spreadValue = (priceA.value - priceB.value).abs
    Spread(priceA, priceB, spreadValue, Instant.now())
  }

  private def removeSpreadCache(spread: Spread): ZIO[SpreadCache with PriceCache, MarketError, Unit] =
    for {
      priceCache <- ZIO.service[PriceCache]
      spreadCache <- ZIO.service[SpreadCache]
      _ <- priceCache.remove(spread.priceA.asset)
      _ <- priceCache.remove(spread.priceB.asset)
      _ <- spreadCache.remove(Spread.toAssetSpread(spread))
    } yield ()

  private def saveSpread(spread: Spread): ZIO[MarketRepository, MarketError, AssetId] =
    for {
      repo <- ZIO.service[MarketRepository]
      assetId <- repo.saveSpread(spread)
      _ <- removeSpreadCache(spread)
      _ <- ZIO.logInfo(s"Spread saved with id: ${assetId}")
    } yield id

  override def execute(cmd: SpreadCommand): ZIO[Environment, MarketError, Spread] = {
    val normalizedIds = AssetSpreadId.normalize(cmd.assetA, cmd.assetB)

    for {
      env <- ZIO.environment[Environment]
      _ <- ZIO.logInfo(s"Executing spread command for $normalizedIds")

      priceA <- getPrice(normalizedIds.assetA)
      priceB <- getPrice(normalizedIds.assetB)

      spread = getSpread(priceA, priceB)
      _ <- saveSpread(spread)

      config <- ZIO.service[ConfigService]
      _ <- ZIO.succeed(updateState(env.getState, spread, config.spreadThreshold))
    } yield spread
  }
}
