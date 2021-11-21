name := "cfor"

val cforVersion = "0.2"

scalaVersion := "2.13.7"
version := cforVersion

sonatypeProfileName := "io.github.metarank"

lazy val sharedSettings = Seq(
  crossScalaVersions := Seq("2.12.15", "2.13.7"),
  organization := "io.github.metarank",
  version := cforVersion,
  scalaVersion := "2.13.7",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalatest" %% "scalatest"     % "3.2.10" % "test"
  )
)

lazy val macros = (project in file("macros")).settings(sharedSettings)

lazy val benchmark = (project in file("benchmark")).settings(sharedSettings).dependsOn(macros)
