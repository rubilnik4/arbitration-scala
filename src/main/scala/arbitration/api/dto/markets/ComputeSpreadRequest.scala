package arbitration.api.dto.markets

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class ComputeSpreadRequest(assetIdA: String, assetIdB: String)

object ComputeSpreadRequest {
  given JsonCodec[ComputeSpreadRequest] = DeriveJsonCodec.gen
  given Schema[ComputeSpreadRequest] = DeriveSchema.gen
}