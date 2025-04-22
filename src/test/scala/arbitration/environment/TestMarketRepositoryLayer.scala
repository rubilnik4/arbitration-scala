package arbitration.environment

import arbitration.application.configurations.AppConfig
import arbitration.infrastructure.repositories.{
  MarketRepository,
  PostgresMarketRepositoryLayer
}
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.testcontainers.utility.DockerImageName
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object TestMarketRepositoryLayer {
  private final val postgresVersion = DockerImageName.parse("postgres:15")

  private val postgresLayer: ZLayer[Any, Throwable, PostgreSQLContainer] =
    ZLayer.scoped {
      ZIO.acquireRelease(
        ZIO.attempt {
          val container = new PostgreSQLContainer(Some(postgresVersion))
          container.start()
          container
        }
      )(container => ZIO.attempt(container.stop()).orDie)
    }

  private val dataSourceLayer
      : ZLayer[PostgreSQLContainer, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        container <- ZIO.service[PostgreSQLContainer]
        config = {
          val cfg = new HikariConfig()
          cfg.setJdbcUrl(container.jdbcUrl)
          cfg.setUsername(container.username)
          cfg.setPassword(container.password)
          cfg.setDriverClassName(container.driverClassName)
          cfg
        }
      } yield new HikariDataSource(config)
    }

  val postgresMarketRepositoryLive
      : ZLayer[AppConfig, Throwable, MarketRepository] =
    postgresLayer >+>
      dataSourceLayer >+>
      PostgresMarketRepositoryLayer.migrationLayer >+>
      PostgresMarketRepositoryLayer.quillLayer >+>
      PostgresMarketRepositoryLayer.postgresMarketRepositoryLayer
}
