package arbitration.infrastructure.repositories

import arbitration.domain.entities.PriceEntity
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class PriceDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  private inline def priceTable = quote {
    querySchema[PriceEntity]("prices")
  }

  def insertPrice(price: PriceEntity): ZIO[Any, SQLException, UUID] =
    insertPriceQuote(price)
      .catchSome {
        case _: SQLException =>
          getPriceIdByTimeQuote(price.assetId, price.time).flatMap {
            case head :: _ => ZIO.succeed(head)
            case Nil => ZIO.fail(new SQLException("Not found after insert fail"))
          }
      }

  def getLastPrice(assetId: String): ZIO[Any, SQLException, Option[PriceEntity]] =
    getLastPriceQuote(assetId)
      .map(_.headOption)

  private def insertPriceQuote(price: PriceEntity) =
    run(
      quote {
        priceTable
          .insertValue(lift(price))
          .returning(_.id)
      })

  private def getLastPriceQuote(assetId: String) =
    run(
      quote {
        priceTable
          .filter(_.assetId == lift(assetId))
          .sortBy(_.time)(Ord.desc)
          .take(1)
      })

  private def getPriceIdByTimeQuote(assetId: String, time: Instant) =
    run(
      quote {
        priceTable
          .filter(price => price.assetId == lift(assetId) && price.time == lift(time))
          .take(1)
          .map(_.id)
      })
}




