name := "cfor"

version := "0.1"

scalaVersion := "2.13.5"

crossScalaVersions := Seq("2.12.12", "2.13.5")

lazy val sharedSettings = Seq(
  organization := "me.dfdx",
  scalaVersion := "2.13.5",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalatest" %% "scalatest"     % "3.2.6" % "test"
  )
)

lazy val macros = (project in file("macros")).settings(sharedSettings)

lazy val benchmark = (project in file("benchmark")).settings(sharedSettings).dependsOn(macros)
