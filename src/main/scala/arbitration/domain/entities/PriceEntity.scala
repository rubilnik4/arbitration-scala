package arbitration.domain.entities

import arbitration.domain.models.{AssetId, Price}

import java.time.Instant
import java.util.UUID

final case class PriceEntity(id: UUID, asset: String, value: BigDecimal, time: Instant)

object PriceMapper {
  def toDomain(price: PriceEntity): Price =
    Price(
      asset = AssetId(price.asset),
      value = price.value,
      time = price.time
    )

  def toEntity(price: Price): PriceEntity =
    PriceEntity(
      id = UUID.randomUUID(),
      asset = price.asset.id,
      value = price.value,
      time = price.time
    )
}