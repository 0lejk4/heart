import Build._
import scala.sys.process._

ThisBuild / organization           := "company.jap"
ThisBuild / organizationName       := "Jap"
ThisBuild / idePackagePrefix       := Some("jap.heart")
ThisBuild / startYear              := Some(2024)
ThisBuild / homepage               := Some(url("https://github.com/jap-company/heart"))
ThisBuild / licenses               := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"
ThisBuild / developers             :=
  List(
    Developer(
      "0lejk4",
      "Oleh Dubynskiy",
      "",
      url("https://github.com/0lejk4"),
    )
  )

lazy val modules: Seq[ProjectReference] = List(
  `heart-core`,
  `heart-zio`,
  `heart-cats`,
  `heart-tapir`,
  `heart-admin`,
  `heart-circe`,
  `heart-http4s`,
  `heart-akka-http`,
  `heart-bot`,
  `heart-http4s-client`,
  `heart-scalacache`,
  `heart-doobie`,
  `heart-cassandra`,
  `heart-redis`,
  `heart-log4cats`,
  `heart-sttp`,
  `heart-fs2-kafka`,
)

lazy val modulesDeps = modules.map(ClasspathDependency(_, None))

lazy val root = (project in file("."))
  .aggregate(modules: _*)
  .aggregate(examples)
  .settings(
    name            := "heart",
    scalaVersion    := editorScala,
    publishArtifact := false,
  )

lazy val `heart-core` =
  heartModule("core")
    .settings(buildInfoSettings("jap.heart"))
    .enablePlugins(BuildInfoPlugin)

lazy val `heart-bot` =
  heartModule("bot")
    .settings(
      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-api"    % V.Slf4j,
        "org.slf4j" % "slf4j-simple" % V.Slf4j,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-cats` =
  heartModule("cats")
    .settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core"          % V.Cats,
        "org.typelevel" %% "cats-effect-kernel" % V.CatsEffect,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-zio` =
  heartModule("zio")
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"          % V.Zio,
        "dev.zio" %% "zio-test"     % V.Zio % Test,
        "dev.zio" %% "zio-test-sbt" % V.Zio % Test,
      ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )
    .dependsOn(`heart-core`)

lazy val `heart-tapir` =
  heartModule("tapir")
    .settings(
      libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-core" % V.Tapir
    )
    .dependsOn(`heart-core`)

lazy val `heart-akka-http` =
  heartModule("akka-http")
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http"   % V.AkkaHttp,
        "com.typesafe.akka" %% "akka-stream" % V.Akka % Provided,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-http4s-client` =
  heartModule("http4s-client")
    .settings(
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-client"       % V.Http4s,
        "org.http4s" %% "http4s-ember-server" % V.Http4s,
        // "org.http4s" %% "http4s-ember-client" % V.Http4s,
        "org.http4s" %% "http4s-dsl"          % V.Http4s,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-scalacache` =
  heartModule("scalacache")
    .settings(
      libraryDependencies += "com.github.cb372" %% "scalacache-core" % V.Scalacache
    )
    .dependsOn(`heart-core`)

lazy val `heart-doobie` =
  heartModule("doobie")
    .settings(
      libraryDependencies ++= Seq(
        "org.tpolecat"  %% "doobie-core" % V.Doobie,
        "com.h2database" % "h2"          % V.H2 % Test,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-cassandra` =
  heartModule("cassandra")
    .settings(
      libraryDependencies ++= Seq(
        "com.datastax.oss" % "java-driver-core"               % V.Cassandra,
        "com.dimafeng"    %% "testcontainers-scala-scalatest" % V.TestContainers % Test,
        "com.dimafeng"    %% "testcontainers-scala-cassandra" % V.TestContainers % Test,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-redis` =
  heartModule("redis")
    .settings(
      libraryDependencies += "dev.profunktor" %% "redis4cats-effects" % V.Redis4cats
    )
    .dependsOn(`heart-core`)

lazy val `heart-log4cats` =
  heartModule("log4cats")
    .settings(
      libraryDependencies += "org.typelevel" %% "log4cats-core" % V.Log4Cats
    )
    .dependsOn(`heart-core`)

lazy val `heart-sttp` =
  heartModule("sttp")
    .settings(
      libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % V.Sttp
    )
    .dependsOn(`heart-core`)

lazy val `heart-fs2-kafka` =
  heartModule("fs2-kafka")
    .settings(
      libraryDependencies ++= Seq(
        "com.github.fd4s" %% "fs2-kafka"                      % V.Fs2Kafka,
        "com.dimafeng"    %% "testcontainers-scala-scalatest" % V.TestContainers % Test,
        "com.dimafeng"    %% "testcontainers-scala-kafka"     % V.TestContainers % Test,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-http4s` =
  heartModule("http4s")
    .settings(
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-ember-server" % V.Http4s,
        "org.http4s" %% "http4s-dsl"          % V.Http4s,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-circe` =
  heartModule("circe")
    .settings(
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core"    % V.Circe,
        "io.circe" %% "circe-generic" % V.Circe,
      )
    )
    .dependsOn(`heart-core`)

lazy val `heart-admin` =
  heartModule("admin")
//    .settings {
//      (Compile / compile) := (Compile / compile).dependsOn(buildAdminUI).value
//    }

val heartAdminResourceFolder = "./modules/admin/src/main/resources/heart-admin/"
lazy val buildAdminUI        = taskKey[Unit]("Build HealthCheck Admin UI")
ThisBuild / buildAdminUI := {
  val installExitCode = Process(Seq("yarn", "install"), new File("../admin-ui/")) ! streams.value.log
  if (installExitCode != 0) sys.error(s"yarn install failed with exit code $installExitCode")

  val buildExitCode = Process(Seq("yarn", "build"), new File("../admin-ui/")) ! streams.value.log
  if (buildExitCode != 0) sys.error(s"yarn build failed with exit code $buildExitCode")

  val mkDirCode = Process(Seq("mkdir", "-p", heartAdminResourceFolder), new File(".")) ! streams.value.log
  if (mkDirCode != 0) sys.error(s"mkdir failed with exit code $buildExitCode")

  val cpCode =
    Process(Seq("cp", "-r", "../admin-ui/build/", heartAdminResourceFolder), new File(".")) ! streams.value.log
  if (cpCode != 0) sys.error(s"mkdir failed with exit code $buildExitCode")
}

lazy val examples =
  (project in file("examples"))
    .settings(
      scalaSettings,
      crossScalaVersions := Nil,
      publishArtifact    := false,
      libraryDependencies ++= Seq(
        // TAPIR
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % V.Tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-zio"               % V.Tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % V.Tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % V.Tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % V.Tapir,
        // HTTP4s
        "org.http4s"                  %% "http4s-ember-server"     % V.Http4s,
        "org.http4s"                  %% "http4s-dsl"              % V.Http4s,
        "org.http4s"                  %% "http4s-circe"            % V.Http4s,
        // AKKA
        "com.typesafe.akka"           %% "akka-actor-typed"        % V.Akka,
        "com.typesafe.akka"           %% "akka-stream"             % V.Akka,
        "ch.megard"                   %% "akka-http-cors"          % V.AkkaCors,
        "de.heikoseeberger"           %% "akka-http-circe"         % V.AkkaHttpJson,
        // LOGGING
        "ch.qos.logback"               % "logback-classic"         % V.Logback % Runtime,
        "io.circe"                    %% "circe-core"              % V.Circe,
        "io.circe"                    %% "circe-generic"           % V.Circe,
      ),
    )
    .dependsOn(modulesDeps: _*)

lazy val `heart-docs` =
  project
    .settings(scalaSettings)
    .settings(crossScalaVersions := Nil)
    .settings(
      moduleName                                 := "heart-docs",
      docsSettings,
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(modules: _*),
    )
    .dependsOn(modulesDeps: _*)
    .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)

val updateDocsVariables = taskKey[Unit]("Update docs variables")
lazy val docsSettings   = Seq(
  publishArtifact          := false,
  mdocVariables            := Map(
    "version"              -> latestVersion.value,
    "organization"         -> (LocalRootProject / organization).value,
    "coreModuleName"       -> (`heart-core` / moduleName).value,
    "zioModuleName"        -> (`heart-zio` / moduleName).value,
    "catsModuleName"       -> (`heart-cats` / moduleName).value,
    "scalaPublishVersions" -> {
      val minorVersions = (`heart-core` / crossScalaVersions).value.map(CrossVersion.binaryScalaVersion)
      if (minorVersions.size <= 2) minorVersions.mkString(" and ")
      else minorVersions.init.mkString(", ") ++ " and " ++ minorVersions.last
    },
  ),
  updateDocsVariables      := {
    val file = (LocalRootProject / baseDirectory).value / "website" / "variables.js"

    val fileHeader =
      "// Generated by sbt. Do not edit directly."

    val fileContents =
      mdocVariables.value.toList
        .sortBy { case (key, _) => key }
        .map { case (key, value) => s"  $key: '$value'" }
        .mkString(s"$fileHeader\nmodule.exports = {\n", ",\n", "\n};\n")

    IO.write(file, fileContents)
  },
  cleanFiles += (ScalaUnidoc / unidoc / target).value,
  docusaurusCreateSite     := docusaurusCreateSite.dependsOn(Compile / unidoc).dependsOn(updateDocsVariables).value,
  docusaurusPublishGhpages := docusaurusPublishGhpages
    .dependsOn(Compile / unidoc)
    .dependsOn(updateDocsVariables)
    .value,
  ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
  // format: off
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-doc-source-url", s"https://github.com/jap-company/heart/tree/v${latestVersion.value}€{FILE_PATH}.scala",
    "-sourcepath", (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-doc-title", "heart",
    "-doc-version", s"v${latestVersion.value}",
    "-doc-logo", (LocalRootProject / baseDirectory).value.getAbsolutePath + "/website/static/img/logo.svg",
  )
  // format: on
)

val latestVersion = settingKey[String]("Latest stable released version")
ThisBuild / latestVersion := {
  val snapshot       = (ThisBuild / isSnapshot).value
  val stable         = (ThisBuild / isVersionStable).value
  val currentVersion = (ThisBuild / version).value
  if (!snapshot && stable) currentVersion
  else (ThisBuild / previousStableVersion).value.getOrElse(currentVersion)
}

Global / excludeLintKeys ++= Set(ThisBuild / idePackagePrefix)
