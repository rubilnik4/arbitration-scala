package arbitration.domain.entities

import arbitration.domain.models.{AssetId, Price}

import java.time.Instant
import java.util.UUID

final case class PriceEntity(
  id: UUID,
  assetId: String,
  value: BigDecimal,
  time: Instant
)

object PriceMapper {
  def toDomain(price: PriceEntity): Price =
    Price(
      assetId = AssetId(price.assetId),
      value = price.value,
      time = price.time
    )

  def toEntity(price: Price): PriceEntity =
    PriceEntity(
      id = UUID.randomUUID(),
      assetId = price.assetId.id,
      value = price.value,
      time = price.time
    )
}
