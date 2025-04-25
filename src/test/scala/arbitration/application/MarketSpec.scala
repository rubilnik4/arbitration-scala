package arbitration.application

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.application.queries.handlers.{PriceQueryHandlerLive, SpreadQueryHandlerLive}
import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}
import arbitration.domain.MarketError
import arbitration.domain.models.*
import arbitration.layers.TestAppEnvLayer
import zio.test.TestAspect.sequential
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}


object MarketSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] = suite("Spread integration tests")(
    test("Should save spread successfully") {
      val steps = 3
      for {
        project <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadAssetId = AssetSpreadId(AssetId(project.assets.assetA), AssetId(project.assets.assetB))
        spreadCommand = SpreadCommand(SpreadState.Init(), spreadAssetId)
        spreadHandler = SpreadCommandHandlerLive()

        spreadState <- ZIO.foldLeft(0 until steps)(SpreadState.Init()) {
          case (state, s) =>
            for {
              result <- spreadHandler.execute(SpreadCommand(state, spreadAssetId))
            } yield result.spreadState
        }

      } yield assertTrue(
        spreadState.lastSpread.exists(_ > 0),
        spreadState.spreadHistory.size == steps
      )
    },

    test("Should retrieve last price") {
      for {
        project <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        assetId = AssetId(project.assets.assetA)
        priceQuery = PriceQuery(assetId)
        priceHandler = PriceQueryHandlerLive()

        price <- priceHandler.handle(priceQuery)
      } yield assertTrue(
        price.assetId == assetId,
        price.value > 0
      )
    },

    test("Should retrieve last spread") {
      for {
        project <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadAssetId = AssetSpreadId(AssetId(project.assets.assetA), AssetId(project.assets.assetB))
        spreadQuery = SpreadQuery(spreadAssetId)
        spreadHandler = SpreadQueryHandlerLive()

        spread <- spreadHandler.handle(spreadQuery)
      } yield assertTrue(
        spread.value > 0
      )
    }
  ).provideLayerShared(TestAppEnvLayer.testAppEnvLive) @@ sequential
}
