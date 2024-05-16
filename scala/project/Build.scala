import _root_.io.github.davidgregory084.TpolecatPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys._
import sbtbuildinfo._

object Build {
  lazy val V = new {
    val Doobie         = "1.0.0-RC2"
    val Scalacache     = "1.0.0-M6"
    val Fs2Kafka       = "2.5.0"
    val KindProjector  = "0.13.2"
    val Redis4cats     = "1.3.0"
    val H2             = "2.1.214"
    val Log4Cats       = "2.5.0"
    val Cassandra      = "4.15.0"
    val TestContainers = "0.40.12"
    val Cats           = "2.9.0"
    val CatsEffect     = "3.4.4"
    val Tapir          = "1.2.4"
    val Sttp           = "3.8.6"
    val AkkaHttp       = "10.4.0"
    val Akka           = "2.7.0"
    val AkkaCors       = "1.1.3"
    val AkkaHttpJson   = "1.39.2"
    val Circe          = "0.14.3"
    val Slack          = "0.3.1"
    val Http4s         = "0.23.16"
    val Zio            = "2.0.5"
    val Scala3         = "3.1.2"
    val Scala213       = "2.13.8"
    val Scala212       = "2.12.16"
    val MUnit          = "0.7.29"
    val Logback        = "1.2.10"
    val Slf4j          = "2.0.5"
  }

  val editorScala = V.Scala213

  lazy val scalaSettings = Seq(
    scalaVersion           := editorScala,
    crossScalaVersions     := List(V.Scala212, V.Scala213, V.Scala3),
    tpolecatExcludeOptions := Set(ScalacOptions.privateKindProjector),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-Ykind-projector:underscores", "-Xmax-inlines:64")
        case _            => Seq("-Xsource:3", "-P:kind-projector:underscore-placeholders")
      }
    },
    libraryDependencies ++= (
      if (scalaVersion.value == V.Scala3) List()
      else List(compilerPlugin(("org.typelevel" % "kind-projector" % "0.13.2").cross(CrossVersion.full)))
    ),
  )

  lazy val commonSettings = Seq(
    libraryDependencies += "org.scalameta" %% "munit" % V.MUnit % Test
  ) ++ scalaSettings

  def heartModule(name: String) = (Project(s"heart-$name", file(s"modules/$name"))).settings(commonSettings)

  def buildInfoSettings(packageName: String) =
    Seq(
      buildInfoKeys    := Seq[BuildInfoKey](organization, moduleName, name, version, scalaVersion, isSnapshot),
      buildInfoPackage := packageName,
    )
}
