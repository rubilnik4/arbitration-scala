package arbitration.infrastructure.repositories

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.MarketError.DatabaseError
import arbitration.domain.MarketError.NotFound
import arbitration.domain.entities.{PriceEntity, PriceMapper, SpreadEntity, SpreadMapper}
import arbitration.domain.models.AssetSpreadId.toKey
import arbitration.domain.models.{AssetId, AssetSpreadId, Price, Spread, SpreadId}
import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import zio.*

import java.sql.{SQLException, Timestamp}
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

// Преобразование Instant <-> Timestamp
implicit val encodeInstant: MappedEncoding[Instant, Timestamp] =
  MappedEncoding(instant => Timestamp.from(instant))

implicit val decodeInstant: MappedEncoding[Timestamp, Instant] =
  MappedEncoding(ts => ts.toInstant)
  
final class PostgresMarketRepository extends MarketRepository {
  private def savePrice(price: Price): ZIO[QuillDatabaseContext with DataSource, SQLException, UUID] = {
    val priceEntity = PriceMapper.toEntity(price)

    ZIO.serviceWithZIO[QuillDatabaseContext] { ctx =>
      import ctx._

      val insert = quote {
        query[PriceEntity]
          .insertValue(priceEntity)
          .returning(_.id)
      }

      val select = quote {
        query[PriceEntity]
          .filter(p => p.asset == lift(priceEntity.asset) && p.time == lift(priceEntity.time))
          .map(_.id)
      }

      ctx.run(insert)
        .catchSome {
          case _: SQLException =>
            ctx.run(select).flatMap {
              case head :: _ => ZIO.succeed(head)
              case Nil => ZIO.fail(new SQLException("Price not found after insert fail"))
            }
        }
    }
  }

  override def saveSpread(spread: Spread): ZIO[QuillDatabaseContext with DataSource, MarketError, SpreadId] = {
    (for {
      _ <- ZIO.logDebug(s"Saving spread $spread to database")

      priceAId <- savePrice(spread.priceA)
      priceBId <- savePrice(spread.priceB)

      spreadEntity = SpreadMapper.toEntity(spread, priceAId, priceBId)

      spreadId <- ZIO.serviceWithZIO[QuillDatabaseContext] { ctx =>
        import ctx._

        val insert = quote {
          query[SpreadEntity]
            .insertValue(lift(spreadEntity))
            .returning(_.id)
        }

        val select = quote {
          query[SpreadEntity]
            .filter(s => s.assetSpreadId == lift(spreadEntity.assetSpreadId) && s.time == lift(spreadEntity.time))
            .map(_.id)
        }

        ctx.run(insert)
          .catchSome {
            case _: SQLException =>
              ctx.run(select).flatMap {
                case head :: _ => ZIO.succeed(head)
                case Nil => ZIO.fail(new SQLException("Spread not found after insert fail"))
              }
          }

      }
    } yield SpreadId(spreadId))
      .tapError(e => ZIO.logErrorCause(s"Failed to save spread $spread to database", Cause.fail(e)))
      .mapError {
        DatabaseError(s"Failed to save spread $spread to database", _: SQLException)
      }
  }

  override def getLastPrice(assetId: AssetId): ZIO[QuillDatabaseContext with DataSource, MarketError, Price] = {
    ZIO.serviceWithZIO[QuillDatabaseContext] { ctx =>
      import ctx._
      val assetKey = assetId.id

      val select = quote {
        query[PriceEntity]
          .filter(_.asset == lift(assetKey))
          .sortBy(_.time)(Ord.desc)
          .take(1)
      }

      ctx.run(select)
        .flatMap {
          case head :: _ => ZIO.succeed(PriceMapper.toDomain(head))
          case Nil => ZIO.fail(NotFound(s"Price for asset '$assetId' not found"))
        }
        .mapError {
          case sql: SQLException => DatabaseError("Failed to load price", sql)
        }
    }
  }

  override def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[QuillDatabaseContext with DataSource, MarketError, Spread] = {
    ZIO.serviceWithZIO[QuillDatabaseContext] { ctx =>
      import ctx._
      val spreadKey = toKey(assetSpreadId)

      val select = quote {
        query[SpreadEntity]
          .join(query[PriceEntity]).on((s, pa) => s.priceAId == pa.id)
          .join(query[PriceEntity]).on { case ((s, pa), pb) => s.priceBId == pb.id }
          .filter { case ((s, _), _) => s.assetSpreadId == lift(spreadKey) }
          .sortBy { case ((s, _), _) => s.time }(Ord.desc)
          .take(1)
          .map {
            case ((s, pa), pb) =>
              Spread(
                value = s.value,
                time = s.time,
                priceA = Price(AssetId(pa.asset), pa.value, pa.time),
                priceB = Price(AssetId(pb.asset), pb.value, pb.time)
              )
          }
      }

      ctx.run(select)
        .flatMap {
          case head :: _ => ZIO.succeed(head)
          case Nil => ZIO.fail(NotFound(s"Spread for assets '$assetSpreadId' not found"))
        }
        .mapError {
          case sql: SQLException => DatabaseError("Failed to load spread", sql)
        }
    }
  }
}

