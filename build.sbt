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
      "dev.zio" %% "zio-json" % "0.7.42",
      "dev.zio" %% "zio-cache" % "0.2.4",
      "dev.zio" %% "zio-test" % "2.1.17" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.1.17" % Test,
      "dev.zio" %% "zio-config" % "4.0.4",
      "dev.zio" %% "zio-config-magnolia" % "4.0.4",
      "dev.zio" %% "zio-config-typesafe" % "4.0.4",
      "dev.zio" %% "zio-opentelemetry" % "3.1.4",
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.43.0" % Test,
      "org.postgresql" % "postgresql" % "42.7.5",
      "org.flywaydb" % "flyway-core" % "11.8.0",
      "org.flywaydb" % "flyway-database-postgresql" % "11.8.0",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
      "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.11.0",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.11.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.49.0",
      "io.opentelemetry" % "opentelemetry-sdk-trace" % "1.49.0",
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.49.0",
      "io.opentelemetry" % "opentelemetry-exporter-logging-otlp" % "1.49.0",
      "io.opentelemetry.semconv" % "opentelemetry-semconv" % "1.32.0",
      "org.slf4j" % "slf4j-nop" % "2.0.17"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions += "-Dquill.macro.log=false"
  )