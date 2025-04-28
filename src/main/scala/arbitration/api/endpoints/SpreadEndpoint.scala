package arbitration.api.endpoints

import arbitration.api.dto.markets.*
import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.environments.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, AssetSpreadId, Spread, SpreadState}
import arbitration.application.queries.queries.{PriceQuery, SpreadQuery}
import zio.ZIO
import zio.http.Method.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint


object SpreadEndpoint {
  private final val path = "spread"
  
  private val getSpreadEndpoint =
    Endpoint(GET / path)
      .query(HttpCodec.query[String]("assetIdA"))
      .query(HttpCodec.query[String]("assetIdB"))
      .out[SpreadResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )

  val getSpreadRoute: Route[AppEnv, Nothing] = getSpreadEndpoint.implement { (assetIdA, assetIdB) =>
    val assetSpreadId = AssetSpreadId(AssetId(assetIdA), AssetId(assetIdB))
    val spreadQuery = SpreadQuery(assetSpreadId)
    for {
      spreadQueryHandler <- ZIO.serviceWith[AppEnv](_.marketQueryHandler.spreadQueryHandler)
      result <- spreadQueryHandler.handle(spreadQuery)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          spread => SpreadMapper.toResponse(spread))
    } yield result
  }

  private val computeSpreadEndpoint = {
    Endpoint(POST / path)
      .in[ComputeSpreadRequest](MediaType.application.json)
      .out[SpreadResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
  }

  val computeSpreadRoute: Route[AppEnv, Nothing] = computeSpreadEndpoint.implement { request =>
    val assetSpreadId = AssetSpreadId(AssetId(request.assetIdA), AssetId(request.assetIdB))
    val spreadCommand = SpreadCommand(SpreadState.Init(), assetSpreadId)
    for {
      spreadCommandHandler <- ZIO.serviceWith[AppEnv](_.marketCommandHandler.spreadCommandHandler)
      result <- spreadCommandHandler.handle(spreadCommand)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          spreadResult => SpreadMapper.toResponse(spreadResult.spread))
    } yield result
  }
}
