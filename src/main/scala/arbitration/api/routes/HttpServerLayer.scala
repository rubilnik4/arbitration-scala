package arbitration.api.routes

import arbitration.layers.AppEnv
import zio.{ZIO, ZLayer}
import zio.http.*
import zio.http.Server

object HttpServerLayer {
  val httpServerLive: ZLayer[Routes[AppEnv, Response] & AppEnv & Server, Throwable, Unit] =
    ZLayer.scoped {
      for {
        routes <- ZIO.service[Routes[AppEnv, Response]]
        _ <- Server.serve(routes)
      } yield ()
    }
}
