package arbitration

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId}
import arbitration.environment.ConfigLayer
import arbitration.infrastructure.db.Migration
import arbitration.infrastructure.markets.BinanceMarketApi
import arbitration.infrastructure.markets.BinanceMarketApiSpec.{suite, test}
import arbitration.infrastructure.repositories.{MarketRepository, PostgresMarketRepository}
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import io.getquill.jdbczio.Quill
import org.testcontainers.utility.DockerImageName
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Scope, ZIO, ZLayer}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import javax.sql.DataSource


object SpreadSpec extends ZIOSpecDefault {
  private val postgresVersion = DockerImageName.parse("postgres:15")

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

  private val dataSourceLayer: ZLayer[PostgreSQLContainer, Throwable, DataSource] =
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

  private val migrationLayer: ZLayer[DataSource, Nothing, Unit] =
    ZLayer.fromZIO(
      ZIO.serviceWithZIO[DataSource](ds =>
        Migration.applyMigrations(ds).orDie
      )
    )

  private val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    ZLayer.fromZIO(
      ZIO.serviceWithZIO[DataSource](ds =>
        ZIO.succeed(new Quill.Postgres(SnakeCase, ds))
      )
    )

  val marketRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, MarketRepository] =
    ZLayer.fromFunction(quill => new PostgresMarketRepository(quill))

  object Layers {
    // Слой для репозитория
    val marketRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, MarketRepository] =
      ZLayer.fromFunction(quill => new PostgresMarketRepository(quill))
  }

  object Env {

    val live: ZLayer[Any, Throwable, AppEnv] =
      ZLayer.make[AppEnv](
        // Предполагаемые базовые слои
        ConfigLayer.appConfig, // Слой конфигурации
        MarketCache.live, // Слой кэша
        Layers.quillLayer, // Слой Quill
        Layers.marketRepositoryLayer, // Наш репозиторий
        MarketData.live, // Слой данных рынка
        MarketApi.live // Слой API
      )
  }

//  def spec = suite("Spread integration tests")(
//    test("Should save spread successfully") {
//      for {
//        env <- ZIO.service[AppEnv]
//        project = env.Infra.Config.Project
//        spreadAssetId = AssetSpreadId(project.Assets.AssetA, project.Assets.AssetB)
//        resultWithState <- spreadCommand(env)(SpreadState.Init, spreadAssetId)
//        (result, _) = resultWithState
//      } yield result match {
//        case Right(spread) =>
//          assertTrue(
//            spread.Value > 0,
//            spread.Value == (spread.PriceA.Value - spread.PriceB.Value).abs,
//            getAssetSpreadId(spread) == normalizeSpreadAsset(spreadAssetId)
//          )
//        case Left(err) => fail(s"Failed to compute spread: $err")
//      }
//    },
//
//    test("Should retrieve last price for asset") {
//      for {
//        env <- ZIO.service[AppEnv]
//        asset = env.Infra.Config.Project.Assets.AssetA
//        result <- getLastPrice(env.Infra, asset)
//      } yield result match {
//        case Right(price) =>
//          assertTrue(
//            price.Asset == asset,
//            price.Value > 0
//          )
//        case Left(err) => fail(s"Failed to get last price: $err")
//      }
//    },
//
//    test("Should retrieve last spread") {
//      for {
//        env <- ZIO.service[AppEnv]
//        spreadAssetId = AssetSpreadId(env.Infra.Config.Project.Assets.AssetA, env.Infra.Config.Project.Assets.AssetB)
//        result <- getLastSpread(env.Infra, spreadAssetId)
//      } yield result match {
//        case Right(spread) =>
//          assertTrue(spread.Value > 0)
//        case Left(err) => fail(s"Failed to get last spread: $err")
//      }
//    }
//  ).provideLayerShared(testEnvLayer) @@ sequential
}
