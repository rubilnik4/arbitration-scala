package arbitration.infrastructure.markets

import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import arbitration.infrastructure.markets.BinanceMarketApiSpec.test
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.*
import zio.test.*
import zio.test.TestAspect.*

object BinanceMarketApiSpec extends ZIOSpecDefault {
  private val assetId = AssetId("BTCUSDT_250627")

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("BinanceMarketApi integration")(
      test("should fetch BTCUSDT price") {
        ZIO.scoped {
          for {
            backend <- AsyncHttpClientZioBackend.scoped()
            api = new BinanceMarketApi(backend)
            price <- api.getPrice(assetId)
          } yield assertTrue(price.asset == assetId)
        }
      },
      test("should fail for nonexistent symbol") {
        ZIO.scoped {
          for {
            backend <- AsyncHttpClientZioBackend.scoped()
            api = new BinanceMarketApi(backend)
            result <- api.getPrice(AssetId("FAKESYMBOL")).either
          } yield result match {
            case Left(MarketError.ApiError(provider, code, msg)) =>
              assertTrue(code == -1121, msg.contains("Invalid symbol"))
            case other =>
              assertTrue(false)
          }
        }
      }
    )
}
