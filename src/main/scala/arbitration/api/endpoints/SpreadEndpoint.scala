package arbitration.api.endpoints

import arbitration.api.dto.markets.*
import arbitration.application.commands.commands.SpreadCommand
import arbitration.application.environments.AppEnv
import arbitration.application.queries.queries.SpreadQuery
import arbitration.domain.models.{AssetId, AssetSpreadId, SpreadState}
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object SpreadEndpoint {
  private final val path = "spread"
  private final val tag = "spread"

  private val getSpreadEndpoint =
    Endpoint(GET / path / string("assetIdA") / string("assetIdB"))
      .out[SpreadResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
      .tag(tag)

  private val getSpreadRoute = getSpreadEndpoint.implement { (assetIdA, assetIdB) =>
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
      .tag(tag)
  }

  private val computeSpreadRoute = computeSpreadEndpoint.implement { request =>
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

  val allEndpoints: List[Endpoint[_, _, _, _, _]] =
    List(getSpreadEndpoint, computeSpreadEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(getSpreadRoute, computeSpreadRoute)
}
