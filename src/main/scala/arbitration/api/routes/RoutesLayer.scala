package arbitration.api.routes

import arbitration.api.endpoints.{PriceEndpoint, SpreadEndpoint}
import arbitration.application.environments.AppEnv
import zio.ZLayer
import zio.http.*
import zio.http.codec.*
import zio.http.codec.PathCodec.path
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}

object RoutesLayer {
  private val openApiSpec = OpenAPIGen.fromEndpoints(
    title = "Arbitration Market API",
    version = "1.0.0",
    endpoints = PriceEndpoint.allEndpoints.concat(SpreadEndpoint.allEndpoints) 
  )
  
  private val swaggerRoute =
    SwaggerUI.routes("docs" / "openapi", openApiSpec)
  
  val apiRoutesLive: ZLayer[AppEnv, Throwable, Routes[AppEnv, Response]] =
    ZLayer.fromFunction { (env: AppEnv) =>
      PriceEndpoint.allRoutes ++ SpreadEndpoint.allRoutes ++ swaggerRoute
    }
}
