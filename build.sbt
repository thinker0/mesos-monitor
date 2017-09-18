import sbt.Keys.{isSnapshot, publishMavenStyle, _}
import sbt._

name := "mesos-monitor"
organization := "com.github.thinker0"

scalaVersion := "2.12.3"

version := (version in ThisBuild).value

isSnapshot := version.value.endsWith("-SNAPSHOT")

updateOptions := updateOptions.value.withCachedResolution(false)

parallelExecution in ThisBuild := false

lazy val versions = new {
  val guice = "4.1.0"
  val finagle = "7.1.0"
  val logback = "1.2.3"
  val slf4j = "1.7.25"
  val jackson = "2.9.1"
  val curator = "2.11.0"
  val sentry = "1.1.0"
  val heron = "0.15.2"
  val protoBuf = "3.4.0"
  val grpc = "1.6.1"
  val scalaPb = "0.6.3"
}

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  version := (version in ThisBuild).value,
  isSnapshot := version.value.endsWith("-SNAPSHOT"),
  scalaVersion := "2.12.3",
  scalacOptions := Seq(
    // Note: Add -deprecation when deprecated methods are removed
    "-target:jvm-1.8",
    "-unchecked",
    "-feature",
    "-language:_",
    "-encoding", "utf8",
    "-Xlint:-missing-interpolator",
    "-Ypatmat-exhaust-depth", "40"
  ),
  javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
  javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native",
    "-server", "-Xmx1024m", "-Xms1024m", "-Xss256k", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=20",
    "-Xlint:unchecked", "-Xlint:deprecation",
    "-Dlog.service.output=/dev/stderr",
    "-Dlog.access.output=/dev/stderr"),
  resolvers := Seq(
    Resolver.sonatypeRepo("releases"),
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Twitter Maven" at "https://maven.twttr.com"
  ),
  // dependencies
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % versions.slf4j,
    "org.slf4j" % "log4j-over-slf4j" % versions.slf4j,
    "org.slf4j" % "jcl-over-slf4j" % versions.slf4j,
    "org.slf4j" % "jul-to-slf4j" % versions.slf4j,

    "com.google.inject" % "guice" % versions.guice,
    "com.google.inject.extensions" % "guice-multibindings" % versions.guice,
    "com.google.inject.extensions" % "guice-assistedinject" % versions.guice,

    "com.twitter" %% "finagle-http" % versions.finagle,
    "com.twitter" %% "finagle-http2" % versions.finagle,

    "com.fasterxml.jackson.core" % "jackson-databind" % versions.jackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % versions.jackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % versions.jackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % versions.jackson,
    "com.fasterxml.jackson.module" % "jackson-module-afterburner" % versions.jackson,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson,

    "io.gatling" % "jsonpath_2.12" % "0.6.9"
  ).map {
    _
      .exclude("log4j", "log4j")
      .exclude("org.slf4j", "slf4j-jdk14")
      .exclude("org.slf4j", "slf4j-log4j12")
      .exclude("commons-logging", "commons-logging")
  },
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := Some(Resolver.file("file", new File("target/release/"))),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  autoAPIMappings := true
)

lazy val projectList = Seq[sbt.ProjectReference](
  `mesos-idl`,
  `mesos-metrics`
)

def defineProject(n: String): Project = {
  val name = "mesos-" + n
  Project(
    id = name,
    base = file(name),
    settings = Defaults.itSettings ++ buildSettings
  ).configs(IntegrationTest)
}

lazy val `mesos-idl` = defineProject("idl")
  .settings(
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-protobuf" % versions.grpc,
      "io.grpc" % "grpc-netty" % versions.grpc,
      "io.grpc" % "grpc-stub" % versions.grpc,

      // "com.trueaccord.scalapb" %% "scalapb-json4s" % versions.scalaPb,
      "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,

      "com.google.protobuf" % "protobuf-java" % versions.protoBuf % "protobuf",
      "com.mesosphere.mesos.rx.java" % "mesos-rxjava-protobuf-client" % "0.2.0",
      "io.grpc" % "grpc-testing" % versions.grpc % "test",
      "org.mockito" % "mockito-core" % "1.9.5" % "test"

    ),
    libraryDependencies ~= {
      _.map(
        _
          .exclude("log4j", "log4j")
          .exclude("org.slf4j", "slf4j-log4j12")
          .exclude("org.slf4j", "slf4j-jdk14")
          .exclude("commons-logging", "commons-logging")
      )
    },
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )

lazy val `mesos-metrics` = defineProject("metrics")
  .settings(
    mainClass := Some("com.github.thinker0.mesos.MesosMetricCollector"),
    assemblyJarName in assembly := s"mesos-metric-topology-${version.value}.jar",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % versions.logback,
      "ch.qos.logback" % "logback-classic" % versions.logback % "test",

      "io.sentry" % "sentry-logback" % versions.sentry,
      "com.esotericsoftware" % "kryo" % "3.0.3",
      "com.twitter.heron" % "heron-storm" % versions.heron
    ),
    libraryDependencies ~= {
      _.map(
        _
          .exclude("log4j", "log4j")
          .exclude("org.slf4j", "slf4j-log4j12")
          .exclude("org.slf4j", "slf4j-jdk14")
          .exclude("commons-logging", "commons-logging")
      )
    },
    // assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript), includeScala = true),
    assemblyMergeStrategy in assembly := {
      case x: String if Seq("BUILD", "NOTICE", "LICENSE", "rootdoc.txt") contains x =>
        MergeStrategy.discard
      case PathList("META-INF", xs@_*) =>
        val discardList = Seq("manifest.mf", "notice", "notice.txt", "rootdoc.txt", "index.list", "dependencies", "license.txt", "license", "build")
        xs map {
          _.toLowerCase
        } match {
          case (x :: Nil) if discardList contains x =>
            MergeStrategy.discard
          case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
            MergeStrategy.discard
          case ps@(x :: xs) if ps.last.endsWith(".properties") =>
            MergeStrategy.last
          case "maven" :: xs =>
            MergeStrategy.last
          case "plexus" :: xs =>
            MergeStrategy.discard
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.deduplicate
        }
      case _ =>
        MergeStrategy.last
    }
  ).dependsOn(`mesos-idl`)

lazy val legibility = Project(
  id = "mesos-monitor",
  base = file("."),
  settings = buildSettings
).aggregate(projectList: _*)
