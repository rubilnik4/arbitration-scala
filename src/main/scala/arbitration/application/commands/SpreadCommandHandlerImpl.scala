package arbitration.application.commands

import arbitration.application.AppEnv
import arbitration.application.commands.SpreadCommandHandler
import arbitration.application.queries.MarketData
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread, SpreadState}
import arbitration.infrastructure.MarketRepository
import arbitration.infrastructure.caches.{PriceCache, SpreadCache}
import zio.ZIO

import java.time.Instant

class SpreadCommandHandlerImpl extends SpreadCommandHandler {
  private def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] =
    for {
      marketData <- ZIO.serviceWith[AppEnv](_.marketData)
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

  private def removeSpreadCache(spread: Spread): ZIO[AppEnv, MarketError, Unit] =
    for {
      priceCache <- ZIO.serviceWith[AppEnv](_.priceCache)
      spreadCache <- ZIO.serviceWith[AppEnv](_.spreadCache)
      _ <- priceCache.remove(spread.priceA.asset)
      _ <- priceCache.remove(spread.priceB.asset)
      _ <- spreadCache.remove(Spread.toAssetSpread(spread))
    } yield ()

  private def saveSpread(spread: Spread): ZIO[AppEnv, MarketError, AssetId] =
    for {
      marketRepository <- ZIO.serviceWith[AppEnv](_.marketRepository)
      assetId <- marketRepository.saveSpread(spread)
      _ <- removeSpreadCache(spread)
      _ <- ZIO.logInfo(s"Spread saved with id: $assetId")
    } yield assetId

  private def updateState(state: SpreadState, spread: Spread, threshold: BigDecimal): SpreadState = {
    val updatedHistory = (spread.value :: state.spreadHistory).take(threshold.toInt)
    val isExceeded = spread.value > threshold
    if (isExceeded) logger.info(s"Threshold $threshold exceeded for spread ${spread.priceA.asset}-${spread.priceB.asset}")
    state.copy(
      lastSpread = Some(spread.value),
      spreadHistory = updatedHistory,
      isThresholdExceeded = isExceeded
    )
  }

  override def execute(cmd: SpreadCommand): ZIO[AppEnv, MarketError, Spread] = {
    val normalizedIds = AssetSpreadId.normalize(cmd.assetA, cmd.assetB)

    for {
      env <- ZIO.environment[AppEnv]
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
