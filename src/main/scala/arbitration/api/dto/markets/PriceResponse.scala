package arbitration.api.dto.markets

import arbitration.domain.models.Price
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class PriceResponse(assetId: String, value: BigDecimal, time: Instant)

object PriceResponse {
  given JsonCodec[PriceResponse] = DeriveJsonCodec.gen
  given Schema[PriceResponse] = DeriveSchema.gen
}

object PriceMapper {
  def toResponse(price: Price): PriceResponse =
    PriceResponse(      
      assetId = price.assetId.id,
      value = price.value,
      time = price.time
    )
}