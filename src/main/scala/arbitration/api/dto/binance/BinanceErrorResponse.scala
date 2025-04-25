package arbitration.api.dto.binance

import zio.json.JsonDecoder

final case class BinanceErrorResponse(code: Int, msg: String)
    derives JsonDecoder
