ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "arbitration-scala"
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.17",
  "dev.zio" %% "zio-streams" % "2.1.17",
  "dev.zio" %% "zio-json" % "0.7.42",
  "dev.zio" %% "zio-http" % "3.2.0",
  "dev.zio" %% "zio-logging" % "2.5.0",
  "dev.zio" %% "zio-test" % "2.1.17" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.17" % Test
)