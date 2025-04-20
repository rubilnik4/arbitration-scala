package arbitration.application.dto

import zio.json.{DeriveJsonDecoder, JsonDecoder}

final case class BinancePriceResponse(symbol: String, price: String, time: Long) derives JsonDecoder
