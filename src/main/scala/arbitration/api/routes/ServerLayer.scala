package arbitration.api.routes

import arbitration.application.environments.AppEnv
import zio.{ZIO, ZLayer}
import zio.http.{Response, Routes, Server}

object ServerLayer {
  val serverLive: ZLayer[AppEnv & Routes[AppEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        routes <- ZIO.service[Routes[AppEnv, Response]]
        server <- Server.serve(routes).provideSomeLayer[AppEnv & Routes[AppEnv, Response]](
          Server.defaultWithPort(8080)
        )
      } yield server
    }
}
