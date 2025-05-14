package arbitration.api.endpoints

import arbitration.api.dto.markets.*
import arbitration.application.queries.queries.PriceQuery
import arbitration.domain.models.AssetId
import arbitration.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint


object PriceEndpoint {
  private final val path = "price"
  private final val tag = "price"

  private val getPriceEndpoint =
    Endpoint(GET / path / string("assetId"))
      .out[PriceResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
      .tag(tag)
      
  private val getPriceRoute: Route[AppEnv, Nothing] = getPriceEndpoint.implement { assetId =>
    val priceQuery = PriceQuery(AssetId(assetId))
    for {
      _ <- ZIO.logInfo(s"Received request for assert price: $assetId")
      
      priceQueryHandler <- ZIO.serviceWith[AppEnv](_.marketQueryHandler.priceQueryHandler)
      result <- priceQueryHandler.handle(priceQuery)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          price => PriceMapper.toResponse(price))
    } yield result
  }

  val allEndpoints: List[Endpoint[_, _, _, _, _]] =
    List(getPriceEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(getPriceRoute)
}
