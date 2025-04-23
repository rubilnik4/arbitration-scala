package arbitration

import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.commands.handlers.SpreadCommandHandlerLive
import arbitration.application.environments.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.*
import arbitration.layers.TestAppEnvLayer
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}


object SpreadSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] = suite("Spread integration tests")(
    test("Should save spread successfully") {
      for {
        env <- ZIO.service[AppEnv]
        project = env.appConfig.project
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
  ).provideLayerShared(TestAppEnvLayer.testAppEnvLive) //@@ sequential
}
