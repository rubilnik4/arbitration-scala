package arbitration.infrastructure.repositories

import arbitration.domain.entities.{PriceEntity, SpreadEntity, SpreadPricesEntity}
import io.getquill.*
import io.getquill.jdbczio.*
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  private inline def priceTable = quote {
    querySchema[PriceEntity](MarketTableNames.Prices)
  }

  private inline def spreadTable = quote {
    querySchema[SpreadEntity](MarketTableNames.Spreads)
  }

  def insertSpread(spread: SpreadEntity): ZIO[Any, SQLException, UUID] =
    insertSpreadQuote(spread)
      .catchSome {
        case _: SQLException =>
          getSpreadIdByTimeQuote(spread.assetSpreadId, spread.time).flatMap {
            case head :: _ => ZIO.succeed(head)
            case Nil => ZIO.fail(new SQLException("Not found after insert fail"))
          }
      }

  def getLastSpread(assetSpreadId: String): ZIO[Any, SQLException, Option[SpreadPricesEntity]] =
    getLastSpreadQuote(assetSpreadId)
      .map(_.headOption)

  private def insertSpreadQuote(spread: SpreadEntity) =
    run(
      quote {
        spreadTable
          .insertValue(lift(spread))
          .returning(_.id)
      })

  private def getLastSpreadQuote(assetSpreadId: String) =
    run(
      quote {
        spreadTable
          .join(priceTable)
          .on((spread, priceA) => spread.priceAId == priceA.id)
          .join(priceTable)
          .on { case ((spread, _), priceB) => spread.priceBId == priceB.id }
          .filter { case ((spread, _), _) => spread.assetSpreadId == lift(assetSpreadId) }
          .sortBy { case ((spread, _), _) => spread.time }(Ord.desc)
          .take(1)
          .map { case ((spread, priceA), priceB) => SpreadPricesEntity(spread, priceA, priceB) }
      })

  private def getSpreadIdByTimeQuote(assetSpreadId: String, time: Instant) =
    run(
      quote {
        spreadTable
          .filter(spread => spread.assetSpreadId == lift(assetSpreadId) && spread.time == lift(time))
          .take(1)
          .map(_.id)
      })
}
