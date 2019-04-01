import scala.sys.process._

val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  organization := "com.github.lolgab",
  scalaVersion := "2.11.12",
  nativeLinkingOptions ++= "python3-config --ldflags".!!.split(' ').filter(_.nonEmpty).map(_.trim).toSeq,
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.1.0-SNAP8" % Test
  ),
  Test / nativeLinkStubs := true
)

lazy val snipy = project
  .settings(commonSettings)
  .enablePlugins(ScalaNativePlugin)

lazy val examples = project
  .settings(commonSettings)
  .dependsOn(snipy)
  .enablePlugins(ScalaNativePlugin)

