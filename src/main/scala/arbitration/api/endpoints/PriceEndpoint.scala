package arbitration.api.endpoints

import arbitration.api.dto.markets.PriceResponse
import arbitration.api.dto.markets.MarketErrorResponse
import arbitration.application.environments.AppEnv
import arbitration.application.queries.handlers.PriceQueryHandler
import arbitration.application.queries.queries.PriceQuery
import arbitration.api.dto.markets.{MarketErrorMapper, PriceMapper}
import arbitration.domain.MarketError
import arbitration.domain.models.AssetId
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.Routes
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint


object PriceEndpoint {

  private val getPriceEndpoint =
    Endpoint(GET / "price")
      .query(HttpCodec.query[String]("assetId"))
      .out[PriceResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )

//  val getPriceRoute: Routes[AppEnv, MarketError] =
//    getPriceEndpoint.implement { asset =>
//      val assetId = AssetId(asset)
//      val priceQuery = PriceQuery(assetId)
//      for {
//        priceQueryHandler <- ZIO.serviceWith[AppEnv](_.priceQueryHandler)
//        price <- priceQueryHandler.handle(priceQuery)
//      } yield PriceResponse(price.assetId.id, price.value, price.time)
//    }
  val getPriceRoute = getPriceEndpoint.implement { assetId =>
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
