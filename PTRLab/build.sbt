ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "PTRLab"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "com.typesafe.play" %% "play-json" % "2.9.3"
)
