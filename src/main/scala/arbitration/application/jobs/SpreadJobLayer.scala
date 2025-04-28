package arbitration.application.jobs

import arbitration.application.environments.AppEnv
import arbitration.domain.models.SpreadState
import zio.{ZIO, ZLayer}

object SpreadJobLayer {
  val spreadJobLayer: ZLayer[AppEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        _ <- ZIO.logInfo("Starting spread job")
        _ <- SpreadJob.spreadJob(SpreadState.Init())
          .forkDaemon
          .withFinalizer(_ => ZIO.logInfo("Spread job stopped"))
      } yield ()
    }
}
