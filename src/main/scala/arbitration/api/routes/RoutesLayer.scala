package arbitration.api.routes

import arbitration.api.endpoints.{PriceEndpoint, SpreadEndpoint}
import arbitration.application.environments.AppEnv
import zio.ZLayer
import zio.http.{Response, Routes}

object RoutesLayer {
  val apiRoutesLive: ZLayer[AppEnv, Throwable, Routes[AppEnv, Response]] =
    ZLayer.fromFunction { (env: AppEnv) =>
      PriceEndpoint.allRoutes ++ SpreadEndpoint.allRoutes
    }
}
