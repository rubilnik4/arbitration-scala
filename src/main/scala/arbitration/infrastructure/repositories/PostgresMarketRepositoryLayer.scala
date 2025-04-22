package arbitration.infrastructure.repositories

import arbitration.application.configurations.AppConfig
import arbitration.infrastructure.db.Migration
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object PostgresMarketRepositoryLayer {
  private val dataSourceLayer: ZLayer[AppConfig, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[AppConfig]
        hikariConfig = {
          val cfg = new HikariConfig()
          cfg.setJdbcUrl(config.postgres.connectionString)
          cfg
        }
      } yield new HikariDataSource(hikariConfig)
    }

  val migrationLayer: ZLayer[DataSource, Nothing, Unit] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[DataSource](dataSource =>
        Migration.applyMigrations(dataSource).orDie
      )
    }

  val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    ZLayer.fromZIO {
      ZIO.serviceWith[DataSource](dataSource =>
        new Quill.Postgres(SnakeCase, dataSource)
      )
    }

  val postgresMarketRepositoryLayer
      : ZLayer[Quill.Postgres[SnakeCase], Nothing, MarketRepository] =
    ZLayer.fromFunction(quill => new PostgresMarketRepositoryLive(quill))

  val postgresMarketRepositoryLive
      : ZLayer[AppConfig, Throwable, MarketRepository] =
    dataSourceLayer >+>
      migrationLayer >+>
      quillLayer >+>
      postgresMarketRepositoryLayer
}
