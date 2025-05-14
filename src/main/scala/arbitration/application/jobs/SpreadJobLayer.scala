package arbitration.application.jobs

import arbitration.domain.models.SpreadState
import arbitration.layers.AppEnv
import zio.{ZIO, ZLayer}

object SpreadJobLayer {
  val spreadJobLive: ZLayer[AppEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        _ <- ZIO.logInfo("Starting spread job")
        fiber <- SpreadJob.spreadJob(SpreadState.Init())
          .forever
          .forkScoped
        _ <- fiber.await
      } yield ()
    }
}