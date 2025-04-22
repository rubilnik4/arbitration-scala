package arbitration.infrastructure.repositories

import arbitration.domain.MarketError
import arbitration.domain.MarketError.{DatabaseError, NotFound}
import arbitration.domain.entities.{
  PriceEntity,
  PriceMapper,
  SpreadEntity,
  SpreadMapper
}
import arbitration.domain.models.*
import arbitration.domain.models.AssetSpreadId.toKey
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class PostgresMarketRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends MarketRepository {
  private def savePrice(price: Price): ZIO[Any, SQLException, UUID] = {
    import quill.*

    val priceEntity = PriceMapper.toEntity(price)

    val insert = quote {
      priceTable
        .insertValue(lift(priceEntity))
        .returning(_.id)
    }

    val select = quote {
      priceTable
        .filter(p => p.asset == lift(priceEntity.asset) && p.time == lift(priceEntity.time))
        .map(_.id)
    }

    run(insert)
      .catchSome { case _: SQLException =>
        run(select).flatMap {
          case head :: _ => ZIO.succeed(head)
          case Nil => ZIO.fail(new SQLException("Price not found after insert fail"))
        }
      }

  }

  override def saveSpread(spread: Spread): ZIO[Any, MarketError, SpreadId] = {
    import quill.*

    (for {      
      priceAId <- savePrice(spread.priceA)
      priceBId <- savePrice(spread.priceB)

      spreadEntity = SpreadMapper.toEntity(spread, priceAId, priceBId)

      spreadId <- {
        val insert = quote {
          spreadTable
            .insertValue(lift(spreadEntity))
            .returning(_.id)
        }

        val select = quote {
          spreadTable
            .filter(s => s.assetSpreadId == lift(spreadEntity.assetSpreadId) && s.time == lift(spreadEntity.time))
            .map(_.id)
        }

        run(insert).catchSome { case _: SQLException =>
          run(select).flatMap {
            case head :: _ => ZIO.succeed(head)
            case Nil =>
              ZIO.fail(new SQLException("Spread not found after insert fail"))
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
    
    val select = quote {
      priceTable
        .filter(_.asset == lift(assetId.id))
        .sortBy(_.time)(Ord.desc)
        .take(1)
    }

    run(select)
      .flatMap {
        case head :: _ => ZIO.succeed(PriceMapper.toDomain(head))
        case Nil => ZIO.fail(NotFound(s"Price for asset '$assetId' not found"))
      }
      .tapError(e =>
        ZIO.logErrorCause(s"Failed to get last price $assetId to database", Cause.fail(e))
      )
      .mapError {
        case sql: SQLException => DatabaseError("Failed to load price", sql)
        case notFound: arbitration.domain.MarketError => notFound
      }
  }

  override def getLastSpread(assetSpreadId: AssetSpreadId): ZIO[Any, MarketError, Spread] = {
    import quill.*

    val select = quote {
      spreadTable
        .join(priceTable)
        .on((s, pa) => s.priceAId == pa.id)
        .join(priceTable)
        .on { case ((s, pa), pb) => s.priceBId == pb.id }
        .filter { case ((s, _), _) =>
          s.assetSpreadId == lift(toKey(assetSpreadId))
        }
        .sortBy { case ((s, _), _) => s.time }(Ord.desc)
        .take(1)
        .map { case ((s, pa), pb) =>
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
        case Nil =>
          ZIO.fail(NotFound(s"Spread for assets '$assetSpreadId' not found"))
      }
      .tapError(e =>
        ZIO.logErrorCause(
          s"Failed to get last spread $assetSpreadId to database",
          Cause.fail(e)
        )
      )
      .mapError {
        case sql: SQLException => DatabaseError("Failed to load spread", sql)
        case notFound: arbitration.domain.MarketError => notFound
      }
  }

  private inline def priceTable = quote {
    querySchema[PriceEntity]("prices")
  }

  private inline def spreadTable = quote {
    querySchema[SpreadEntity]("spreads")
  }
}
