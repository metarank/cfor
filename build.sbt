ThisBuild / dynverVTagPrefix := false
ThisBuild / versionScheme    := Some("early-semver")

ThisBuild / credentials ++= (for {
  user <- sys.env.get("SONATYPE_USERNAME")
  pass <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Central Portal", "central.sonatype.com", user, pass)).toList

lazy val assertTagVersion = taskKey[Unit]("assert that version is derived from an exact git tag")
assertTagVersion := {
  if (isSnapshot.value) sys.error(s"version ${version.value} is not an exact git tag version")
}

def isScala2(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }

lazy val sharedSettings = Seq(
  crossScalaVersions := Seq("2.13.18", "3.9.0"),
  organization       := "io.github.metarank",
  scalaVersion       := "2.13.18",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= (if (isScala2(scalaVersion.value)) {
                             Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
                           } else {
                             Seq.empty[ModuleID]
                           }),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.20" % "test"
  )
)

lazy val root = (project in file("."))
  .settings(sharedSettings)
  .settings(name := "cfor-root", publish / skip := true)
  .aggregate(macros, benchmark)

lazy val macros = (project in file("macros")).settings(sharedSettings)

lazy val benchmark = (project in file("benchmark"))
  .settings(sharedSettings)
  .settings(name := "cfor-benchmark", publish / skip := true)
  .dependsOn(macros)
