package arbitration.api.routes

import zio.ZLayer
import zio.http.Server

object ServerLayer {
  val serverLive: ZLayer[Any, Throwable, Server] =
    Server.default 
}
