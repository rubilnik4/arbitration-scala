package arbitration.api.dto.markets

import arbitration.domain.MarketError
import zio.*
import zio.json.*
import zio.schema.{DeriveSchema, Schema}

import java.sql.SQLException

sealed trait MarketErrorResponse

object MarketErrorResponse {
  case class NotFoundError(message: String) extends MarketErrorResponse

  case class BadRequestError(message: String) extends MarketErrorResponse

  case class InternalServerError(message: String, cause: Option[String] = None)
    extends MarketErrorResponse

  given JsonCodec[MarketErrorResponse] = DeriveJsonCodec.gen
  given Schema[MarketErrorResponse] = DeriveSchema.gen
}

object MarketErrorMapper {
  def toResponse(error: MarketError): MarketErrorResponse = error match {
    case MarketError.NotFound(msg) =>
      MarketErrorResponse.NotFoundError(msg)

    case MarketError.DatabaseError(msg, ex) =>
      MarketErrorResponse.InternalServerError(s"Database error: $msg", Some(ex.getMessage))

    case MarketError.CacheError(msg, ex) =>
      MarketErrorResponse.InternalServerError(s"Cache error: $msg", Some(ex.getMessage))

    case MarketError.ApiError(provider, code, msg) =>
      MarketErrorResponse.InternalServerError(
        s"External API error from $provider with code $code: $msg",
        Some("External API call failed")
      )

    case MarketError.ServiceUnavailable(service, ex) =>
      MarketErrorResponse.InternalServerError(
        s"Service '$service' is currently unavailable",
        Some(ex.getMessage)
      )

    case MarketError.Unknown =>
      MarketErrorResponse.InternalServerError("Unknown error occurred", Some("No additional info"))
  }
}
