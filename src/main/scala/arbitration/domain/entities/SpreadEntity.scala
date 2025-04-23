package arbitration.domain.entities

import arbitration.domain.models.{Price, Spread}

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
    id: UUID,
    assetSpreadId: String,
    priceAId: UUID,
    priceBId: UUID,
    value: BigDecimal,
    time: Instant
)

final case class SpreadPricesEntity(
  spread: SpreadEntity,
  priceA: PriceEntity,
  priceB: PriceEntity
)

object SpreadMapper {
  def toDomain(spread: SpreadPricesEntity): Spread =
    Spread(
      value = spread.spread.value,
      time = spread.spread.time,
      priceA = PriceMapper.toDomain(spread.priceA),
      priceB = PriceMapper.toDomain(spread.priceB),
    )
    
  def toEntity(spread: Spread, priceAId: UUID, priceBId: UUID): SpreadEntity =
    SpreadEntity(
      id = UUID.randomUUID(),
      assetSpreadId = Spread.toKey(spread),
      priceAId = priceAId,
      priceBId = priceBId,
      value = spread.value,
      time = spread.time
    )
}
