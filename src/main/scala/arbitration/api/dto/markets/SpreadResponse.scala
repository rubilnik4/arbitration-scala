package arbitration.api.dto.markets

import arbitration.domain.models.{Price, Spread}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class SpreadResponse( 
  priceA: PriceResponse,
  priceB: PriceResponse,
  value: BigDecimal,
  time: Instant)

object SpreadResponse {
  given JsonCodec[SpreadResponse] = DeriveJsonCodec.gen
  given Schema[SpreadResponse] = DeriveSchema.gen
}

object SpreadMapper {
  def toResponse(spread: Spread): SpreadResponse =
    SpreadResponse(
      priceA = PriceMapper.toResponse(spread.priceA),
      priceB = PriceMapper.toResponse(spread.priceB),
      value = spread.value,
      time = spread.time
    )
}