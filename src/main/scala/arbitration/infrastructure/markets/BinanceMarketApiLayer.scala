package arbitration.infrastructure.markets

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.ZLayer

object BinanceMarketApiLayer {
  val binanceMarketApiLive: ZLayer[Any, Throwable, MarketApi] =
    AsyncHttpClientZioBackend.layer() >>> ZLayer.fromFunction(
      BinanceMarketApi.apply
    )
}
