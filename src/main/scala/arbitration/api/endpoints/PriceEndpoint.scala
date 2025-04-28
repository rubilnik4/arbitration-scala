package arbitration.api.endpoints

import arbitration.api.dto.markets.*
import arbitration.application.environments.AppEnv
import arbitration.application.queries.queries.PriceQuery
import arbitration.domain.models.AssetId
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint


object PriceEndpoint {
  private final val path = "price"

  private val getPriceEndpoint =
    Endpoint(GET / path)
      .query(HttpCodec.query[String]("assetId"))
      .out[PriceResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
      
  val getPriceRoute: Route[AppEnv, Nothing] = getPriceEndpoint.implement { assetId =>
    val priceQuery = PriceQuery(AssetId(assetId))
    for {
      priceQueryHandler <- ZIO.serviceWith[AppEnv](_.marketQueryHandler.priceQueryHandler)
      result <- priceQueryHandler.handle(priceQuery)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          price => PriceMapper.toResponse(price))
    } yield result
  }
}
