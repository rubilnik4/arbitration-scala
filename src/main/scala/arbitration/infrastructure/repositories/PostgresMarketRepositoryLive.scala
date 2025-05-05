package arbitration.infrastructure.repositories

import arbitration.domain.MarketError
import arbitration.domain.MarketError.{DatabaseError, NotFound}
import arbitration.domain.entities.*
import arbitration.domain.models.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

final class PostgresMarketRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends MarketRepository {

  private val priceDao = PriceDao(quill)

  private val spreadDao = SpreadDao(quill)

  override def saveSpread(spread: Spread): ZIO[Any, MarketError, SpreadId] =
    (for {
      priceAId <- savePrice(spread.priceA)
      priceBId <- savePrice(spread.priceB)

      spreadEntity = SpreadMapper.toEntity(spread, priceAId, priceBId)

      spreadId <- saveSpread(spreadEntity)
    } yield spreadId)
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to save spread $spread to database", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully save spread $spread to database")
      )

  private def saveSpread(spreadEntity: SpreadEntity): ZIO[Any, MarketError, SpreadId] =
    spreadDao
      .insertSpread(spreadEntity)
      .mapBoth(
        e => DatabaseError("Failed to save spread", e),
        spreadId => SpreadId(spreadId)
      )

  private def savePrice(price: Price): ZIO[Any, MarketError, UUID] =
    val priceEntity = PriceMapper.toEntity(price)
    priceDao
      .insertPrice(priceEntity)
      .mapError(e => DatabaseError("Failed to save price", e))

  override def getLastPrice(assetId: AssetId): ZIO[Any, MarketError, Price] =
    priceDao.getLastPrice(assetId.id)
      .foldZIO(
        e => ZIO.fail(DatabaseError("Failed to get last price", e)),
        {
          case Some(price) => ZIO.succeed(PriceMapper.toDomain(price))
          case None => ZIO.fail(NotFound(s"Price for asset $assetId not found"))
        }
      )
      .tapBoth(
        _ => ZIO.logDebug(s"Successfully get price $assetId from database"),
        e => ZIO.logErrorCause(s"Failed to get price $assetId from database", Cause.fail(e))
      )

  override def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[Any, MarketError, Spread] =
    val assetSpreadKey = AssetSpreadId.toKey(assetSpreadId)
    spreadDao.getLastSpread(assetSpreadKey)
      .foldZIO(
        error => ZIO.fail(DatabaseError("Failed to get last spread", error)),
        {
          case Some(spread) => ZIO.succeed(SpreadMapper.toDomain(spread))
          case None => ZIO.fail(NotFound(s"Spread for assets $assetSpreadId not found"))
        }
      )
      .tapBoth(
        _ => ZIO.logDebug(s"Successfully get spread $assetSpreadId from database"),
        e => ZIO.logErrorCause(s"Failed to get spread $assetSpreadId from database", Cause.fail(e))
      )
}
