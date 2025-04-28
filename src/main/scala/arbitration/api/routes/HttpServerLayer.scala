package arbitration.api.routes

import arbitration.application.environments.AppEnv
import zio.{ZIO, ZLayer}
import zio.http.*
import zio.http.Server

object HttpServerLayer {
  val httpServerLive: ZLayer[ApiRoutes & AppEnv & Server, Throwable, Unit] =
    ZLayer.scoped {
      for {
        apiRoutes <- ZIO.service[ApiRoutes]     
        _ <- Server.serve(apiRoutes.routes)
          .provide(Server.default)
      } yield ()
    }
}
