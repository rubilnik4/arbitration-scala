package arbitration.api.routes

import arbitration.application.environments.AppEnv
import zio.http.{Response, Routes}

final case class ApiRoutes(routes: Routes[AppEnv, Response])
