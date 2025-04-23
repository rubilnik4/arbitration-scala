package arbitration.infrastructure.repositories.prices

import arbitration.domain.entities.PriceEntity
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.ZIO

import java.sql.SQLException
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
          getLastPriceQuote(price.assetId).flatMap {
            case head :: _ => ZIO.succeed(head.id)
            case Nil => ZIO.fail(new SQLException("Not found after insert fail"))
          }
      }

  def getLastPrice(assetId: String): ZIO[Any, SQLException, Option[PriceEntity]] =
    getLastPriceQuote(assetId)
      .map(_.headOption)

  private def getLastPriceQuote(assetId: String) =
    run(
      quote {
        priceTable
          .filter(_.assetId == lift(assetId))
          .sortBy(_.time)(Ord.desc)
          .take(1)
      })

  private def insertPriceQuote(price: PriceEntity) =
    run(
      quote {
        priceTable
          .insertValue(lift(price))
          .returning(_.id)
      }).debug
}




