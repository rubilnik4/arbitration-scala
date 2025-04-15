package arbitration.application.commands

import arbitration.application.Environment
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Spread}
import zio.ZIO

final case class SpreadCommand(assetA: AssetId, assetB: AssetId)




//  private def getSpread(priceA: Price, priceB: Price): Spread = {
//    val spreadValue = Math.abs(priceA.value - priceB.value)
//    Spread(priceA, priceB, spreadValue, Instant.now())
//  }
//
//  private def getPrice(assetId: AssetId): ZIO[DataService with Logging, MarketError, Price] =
//    for {
//      data    <- ZIO.service[DataService]
//      logger  <- ZIO.service[Logging]
//      result  <- data.getPrice(assetId)
//        .catchAll { _ =>
//          data.getLastPrice(assetId)
//            .tapBoth(
//              _ => logger.error(s"No price available at all ${assetId.value}"),
//              _ => logger.info(s"Using last price for ${assetId.value}")
//            )
//        }
//    } yield result
//
//  private def updateState(state: SpreadState, spread: Spread, threshold: Double): SpreadState = {
//    val updatedHistory = (spread.value :: state.spreadHistory).take(threshold.toInt)
//    val isExceeded = spread.value > threshold
//    if(isExceeded) logger.info(s"Threshold $threshold exceeded for spread ${spread.priceA.asset}-${spread.priceB.asset}")
//    state.copy(
//      lastSpread = Some(spread.value),
//      spreadHistory = updatedHistory,
//      isThresholdExceeded = isExceeded
//    )
//  }
//
//  private def removeFromCache(spread: Spread): ZIO[CacheService, Nothing, Unit] =
//    ZIO.serviceWithZIO[CacheService] { cache =>
//      cache.removePrice(spread.priceA.asset) *>
//        cache.removePrice(spread.priceB.asset) *>
//        cache.removeSpread((spread.priceA.asset, spread.priceB.asset))
//    }
//
//  private def saveSpread(spread: Spread): ZIO[RepositoryService with CacheService with Logging, MarketError, AssetId] =
//    for {
//      repo   <- ZIO.service[RepositoryService]
//      logger <- ZIO.service[Logging]
//      id     <- repo.saveSpread(spread)
//      _      <- removeFromCache(spread)
//      _      <- logger.info(s"Spread saved with id: ${id.value}")
//    } yield id
//

//
//  private def normalizeAssets(a: AssetId, b: AssetId): (AssetId, AssetId) =
//    if(a.value.compareTo(b.value) < 0) (a, b) else (b, a)
//}