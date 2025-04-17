package arbitration.application.commands

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.*
import zio.ZIO

import java.time.Instant

final class SpreadCommandHandlerImpl extends SpreadCommandHandler {
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

  private def saveSpread(spread: Spread): ZIO[AppEnv, MarketError, SpreadId] =
    for {
      marketRepository <- ZIO.serviceWith[AppEnv](_.marketRepository)
      spreadId <- marketRepository.saveSpread(spread)
      _ <- removeSpreadCache(spread)
      _ <- ZIO.logInfo(s"Spread saved with id: $spreadId")
    } yield spreadId

  private def updateState(state: SpreadState, spread: Spread): ZIO[AppEnv, MarketError, SpreadState] =
    for {
      config <- ZIO.serviceWith[AppEnv](_.config.project)
      _      <- ZIO.when(spread.value > config.spreadThreshold) {
        ZIO.logInfo(s"Threshold ${config.spreadThreshold} exceeded for spread $spread")
      }

      updatedHistory = (spread.value :: state.spreadHistory).take(config.maxHistorySize)
      newState = state.copy(
        lastSpread = Some(spread.value),
        spreadHistory = updatedHistory,
        isThresholdExceeded = spread.value > config.spreadThreshold
      )
    } yield newState

  override def execute(cmd: SpreadCommand): ZIO[AppEnv, MarketError, SpreadResult] = {
    val assetSpreadId = AssetSpreadId.normalize(cmd.assetSpreadId)

    for {
      env <- ZIO.environment[AppEnv]
      _ <- ZIO.logInfo(s"Executing spread command for $assetSpreadId")

      priceA <- getPrice(assetSpreadId.assetA)
      priceB <- getPrice(assetSpreadId.assetB)

      spread = getSpread(priceA, priceB)
      _ <- saveSpread(spread)

      newState <- updateState(cmd.spreadState, spread)
    } yield SpreadResult(spread, newState)
  }
}
