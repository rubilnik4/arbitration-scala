package arbitration.api.routes

import arbitration.api.endpoints.{PriceEndpoint, SpreadEndpoint}
import arbitration.application.environments.AppEnv
import zio.ZLayer

object ApiRoutesLayer {
  val apiRoutesLive: ZLayer[AppEnv, Nothing, ApiRoutes] =
    ZLayer.fromFunction { (env: AppEnv) =>
      val routes = PriceEndpoint.allRoutes ++ SpreadEndpoint.allRoutes
      ApiRoutes(routes)
    }
}
