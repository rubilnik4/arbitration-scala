ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "arbitration-scala",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.17",
      "dev.zio" %% "zio-streams" % "2.1.17",
      "dev.zio" %% "zio-json" % "0.7.42",
      "dev.zio" %% "zio-http" % "3.2.0",
      "dev.zio" %% "zio-logging" % "2.5.0",
      "dev.zio" %% "zio-logging-slf4j" % "2.5.0",
      "dev.zio" %% "zio-json" % "0.7.42",
      "dev.zio" %% "zio-cache" % "0.2.4",
      "dev.zio" %% "zio-test" % "2.1.17" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.1.17" % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.43.0" % Test,
      "org.postgresql" % "postgresql" % "42.7.5",
      "org.flywaydb" % "flyway-core" % "11.7.2",
      "org.flywaydb" % "flyway-database-postgresql" % "11.7.2",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
      "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.11.0",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.11.0",
      "ch.qos.logback" % "logback-classic" % "1.5.18"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )