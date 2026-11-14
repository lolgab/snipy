import scala.scalanative.build._

ThisBuild / scalaVersion := "3.3.9"
ThisBuild / crossScalaVersions := Seq("3.3.9", "3.10.1")
ThisBuild / organization := "com.github.lolgab"
ThisBuild / homepage := Some(uri("https://lolgab.github.io/snipy"))
ThisBuild / licenses := List(License.Apache2)
ThisBuild / developers := List(
  Developer(
    "lolgab",
    "Lorenzo Gabriele",
    "lorenzolespaul@gmail.com",
    uri("https://github.com/lolgab")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(uri("https://github.com/lolgab/snipy"), "scm:git@github.com:lolgab/snipy.git")
)

val nativeSettings = Seq(
  nativeConfig ~= {
    _.withLinkingOptions(_ ++ PythonConfig.linkingOptions)
  }
)

lazy val root = project
  .in(file("."))
  .aggregate(snipy, examples)
  .settings(publish / skip := true)

lazy val snipy = project
  .enablePlugins(ScalaNativePlugin)
  .settings(nativeSettings)
  .settings(
    description := "Call Python from Scala Native through the CPython C API.",
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.6" % Test,
    Test / envVars ++= sys.env.get("PYTHONHOME").map("PYTHONHOME" -> _).toMap
  )

lazy val examples = project
  .enablePlugins(ScalaNativePlugin)
  .dependsOn(snipy)
  .settings(nativeSettings)
  .settings(
    publish / skip := true,
    Compile / mainClass := Some("examples.Tour")
  )
