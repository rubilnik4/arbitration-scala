package arbitration.api.dto.binance

import zio.json.{DeriveJsonCodec, JsonCodec, JsonDecoder}

final case class BinanceErrorResponse(code: Int, msg: String)

object BinanceErrorResponse {
  given JsonCodec[BinanceErrorResponse] = DeriveJsonCodec.gen
}