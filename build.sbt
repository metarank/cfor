name := "cfor"

val cforVersion = "0.3"

scalaVersion := "2.13.10"
version      := cforVersion

sonatypeProfileName := "io.github.metarank"

def isScala2(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }

lazy val sharedSettings = Seq(
  crossScalaVersions := Seq("2.12.16", "2.13.10", "3.2.1"),
  organization       := "io.github.metarank",
  version            := cforVersion,
  scalaVersion       := "2.13.10",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= (if (isScala2(scalaVersion.value)) {
                             Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
                           } else {
                             Seq.empty[ModuleID]
                           }),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.17" % "test"
  )
)

lazy val macros = (project in file("macros")).settings(sharedSettings)

lazy val benchmark = (project in file("benchmark")).settings(sharedSettings).dependsOn(macros)
