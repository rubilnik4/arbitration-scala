package arbitration.infrastructure.database

import arbitration.application.configurations.PostgresConfig
import arbitration.infrastructure.repositories.{MarketRepository, PostgresMarketRepositoryLayer}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object MarketRepositoryLayer {
  private val dataSourceLayer: ZLayer[PostgresConfig, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[PostgresConfig]
        hikariConfig = {
          val cfg = new HikariConfig()
          cfg.setJdbcUrl(config.connectionString)
          cfg.setDriverClassName("org.postgresql.Driver")
          cfg
        }
      } yield new HikariDataSource(hikariConfig)
    }

  val postgresMarketRepositoryLive: ZLayer[PostgresConfig, Throwable, MarketRepository] =
    dataSourceLayer >>> ZLayer.scoped {
      for {
        dataSource <- ZIO.service[DataSource]
        _ <- Migration.applyMigrations(dataSource)
        layer =
          PostgresMarketRepositoryLayer.quillLayer >>>
          PostgresMarketRepositoryLayer.postgresMarketRepositoryLayer
        repository <- layer.build.map(_.get[MarketRepository])
      } yield repository
    }
}
