package arbitration

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.application.queries.handlers.{PriceQueryHandlerLive, SpreadQueryHandlerLive}
import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}
import arbitration.domain.MarketError
import arbitration.domain.models.*
import arbitration.layers.TestAppEnvLayer
import zio.test.TestAspect.sequential
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}


object SpreadSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] = suite("Spread integration tests")(
    test("Should save spread successfully") {
      for {
        project <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadAssetId = AssetSpreadId(AssetId(project.assets.assetA), AssetId(project.assets.assetB))
        spreadCommand = SpreadCommand(SpreadState.Init(), spreadAssetId)
        spreadHandler = SpreadCommandHandlerLive()

        spreadResult <- SpreadCommandHandlerLive().execute(spreadCommand)
      } yield assertTrue(
        spreadResult.spread.value > 0,
        Spread.toAssetSpread(spreadResult.spread) == AssetSpreadId.normalize(spreadAssetId),
        spreadResult.spreadState.lastSpread.contains(spreadResult.spread.value)
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
