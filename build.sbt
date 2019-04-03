import scala.sys.process._

val commonSettings = Seq(
  version := "0.0.3-SNAPSHOT",
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
  .settings(publishSettings)
  .enablePlugins(ScalaNativePlugin)

lazy val examples = project
  .settings(commonSettings)
  .dependsOn(snipy)
  .enablePlugins(ScalaNativePlugin)

val publishSettings = Seq(
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lolgab/lolgab"),
      "scm:git@github.com:lolgab/snipy.git"
    )
  ),
  developers := List(
    Developer(
      id    = "lolgab",
      name  = "Lorenzo Gabriele",
      email = "lorenzolespaul@gmail.com",
      url   = url("http://lolgab.github.io")
    )
  ),
  description := "Bridge to execute Python code from Scala Native.",
  licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/lolgab/snipy")),
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  Test / skip in publish := true
)
