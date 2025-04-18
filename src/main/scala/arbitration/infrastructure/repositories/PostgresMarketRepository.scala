package arbitration.infrastructure.repositories

import arbitration.domain.MarketError
import arbitration.domain.MarketError.{DatabaseError, NotFound}
import arbitration.domain.entities.{PriceEntity, PriceMapper, SpreadEntity, SpreadMapper}
import arbitration.domain.models.*
import arbitration.domain.models.AssetSpreadId.toKey
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.{SQLException, Timestamp}
import java.time.Instant
import java.util.UUID

implicit val encodeInstant: MappedEncoding[Instant, Timestamp] =
  MappedEncoding(instant => Timestamp.from(instant))

implicit val decodeInstant: MappedEncoding[Timestamp, Instant] =
  MappedEncoding(ts => ts.toInstant)

final class PostgresMarketRepository(quill: Quill.Postgres[SnakeCase]) extends MarketRepository {
  private def savePrice(price: Price): ZIO[Any, SQLException, UUID] = {
    import quill.*

    val priceEntity = PriceMapper.toEntity(price)

    val insert = quote {
      query[PriceEntity]
        .insertValue(lift(priceEntity))
        .returning(_.id)
    }

    val select = quote {
      query[PriceEntity]
        .filter(p => p.asset == lift(priceEntity.asset) && p.time == lift(priceEntity.time))
        .map(_.id)
    }

    run(insert)
      .catchSome {
        case _: SQLException =>
          run(select).flatMap {
            case head :: _ => ZIO.succeed(head)
            case Nil => ZIO.fail(new SQLException("Price not found after insert fail"))
          }
      }

  }

  override def saveSpread(spread: Spread): ZIO[Any, MarketError, SpreadId] = {
    import quill.*

    (for {
      _ <- ZIO.logDebug(s"Saving spread $spread to database")

      priceAId <- savePrice(spread.priceA)
      priceBId <- savePrice(spread.priceB)

      spreadEntity = SpreadMapper.toEntity(spread, priceAId, priceBId)

      spreadId <- {
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

        run(insert).catchSome {
          case _: SQLException =>
            run(select).flatMap {
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

  override def getLastPrice(assetId: AssetId): ZIO[Any, MarketError, Price] = {
    import quill.*

    ZIO.logDebug(s"Getting last price $assetId from database")
    val select = quote {
      query[PriceEntity]
        .filter(_.asset == lift(assetId.id))
        .sortBy(_.time)(Ord.desc)
        .take(1)
    }

    run(select)
      .flatMap {
        case head :: _ => ZIO.succeed(PriceMapper.toDomain(head))
        case Nil => ZIO.fail(NotFound(s"Price for asset '$assetId' not found"))
      }
      .tapError(e => ZIO.logErrorCause(s"Failed to get last price $assetId to database", Cause.fail(e)))
      .mapError {
        case sql: SQLException => DatabaseError("Failed to load price", sql)
        case notFound: arbitration.domain.MarketError => notFound
      }
  }

  override def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[Any, MarketError, Spread] = {
    import quill.*

    val select = quote {
      query[SpreadEntity]
        .join(query[PriceEntity]).on((s, pa) => s.priceAId == pa.id)
        .join(query[PriceEntity]).on { case ((s, pa), pb) => s.priceBId == pb.id }
        .filter { case ((s, _), _) => s.assetSpreadId == lift(toKey(assetSpreadId)) }
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

    run(select)
      .flatMap {
        case head :: _ => ZIO.succeed(head)
        case Nil => ZIO.fail(NotFound(s"Spread for assets '$assetSpreadId' not found"))
      }
      .tapError(e => ZIO.logErrorCause(s"Failed to get last spread $assetSpreadId to database", Cause.fail(e)))
      .mapError {
        case sql: SQLException => DatabaseError("Failed to load spread", sql)
        case notFound: arbitration.domain.MarketError => notFound
      }
  }
  
}

