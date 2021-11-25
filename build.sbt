ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "1.0"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val akkaVersion = "2.6.17"

lazy val root = (project in file("."))
  .settings(
    name := "akka-http-performance-test-demo",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "org.reactivemongo" %% "reactivemongo" % "1.0.7",
      "io.circe" %% "circe-generic" % "0.14.1",
      "ch.qos.logback" % "logback-classic" % "1.2.7",
      "de.heikoseeberger" %% "akka-http-circe" % "1.38.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    )
  )
  .enablePlugins(SbtTwirl)
  .enablePlugins(JavaAppPackaging)
