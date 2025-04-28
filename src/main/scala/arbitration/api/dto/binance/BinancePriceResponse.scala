package arbitration.api.dto.binance

import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, JsonCodec, JsonDecoder}

final case class BinancePriceResponse(symbol: String, price: String, time: Long)

object BinancePriceResponse {
  given JsonCodec[BinancePriceResponse] = DeriveJsonCodec.gen
}