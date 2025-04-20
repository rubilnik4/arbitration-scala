package arbitration.application.dto

import zio.json.JsonDecoder

final case class BinanceErrorResponse(code: Int, msg: String) derives JsonDecoder
