import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{Dist, outputDirectory, distJvmOptions, distBootClass}
import sbt.ScalaVersion
import scala.Some

object RestapiKernelBuild extends Build {
  val Organization = "hackco"
  val Version = "0.1"
  val ScalaVersion = "2.10.4"

  lazy val Restapi = Project(
    id = "restapi",
    base = file("."),
    settings = defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      libraryDependencies ++= Dependencies.restapiKernel,
      distJvmOptions in Dist := "-Xms256M -Xmx1024M",
      outputDirectory in Dist := file("target/dist"),
      distBootClass in Dist := "hackco.boot.AkkaKernelBoot"
    )
  )

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version := Version,
    scalaVersion := ScalaVersion,
    crossPaths := false,
    organizationName := "Hack Attack",
    organizationHomepage := Some(url("http://hckrnews.com"))
  )

  lazy val defaultSettings = buildSettings ++ Seq(
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-language:postfixOps"),
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    fork in Test := true
  )
}

object Dependencies {

  // Versions
  object V {
    val Akka = "2.3.1"
    val Spray = "1.3.1"
    val SprayJson = "1.2.5"
    val Slick = "2.0.1"
    val SlickExtensions = "2.0.1"
    val Camel = "2.13.0"
  }

  val restapiKernel = Seq(
    "ch.qos.logback"      %  "logback-classic"      % "1.1.1" force,
    "com.typesafe"        %% "scalalogging-slf4j"   % "1.1.0",
    "com.typesafe.akka"   %% "akka-actor"           % V.Akka,
    "com.typesafe.akka"   %% "akka-cluster"         % "2.3.3",
    "com.typesafe.akka"   %% "akka-kernel"          % V.Akka,
    "com.typesafe.akka"   %% "akka-slf4j"           % V.Akka,
    "com.typesafe.akka"   %% "akka-testkit"         % V.Akka,
    "commons-codec"       %  "commons-codec"        % "1.9",
    "commons-dbcp"        %  "commons-dbcp"         % "1.4",
    "io.spray"            %  "spray-can"            % V.Spray,
    "io.spray"            %  "spray-routing"        % V.Spray,
    "io.spray"            %  "spray-testkit"        % V.Spray,
    "io.spray"            %% "spray-json"           % V.SprayJson,
    "org.mockito"         % "mockito-core"          % "1.9.5"   % "test",
    "org.scalacheck"      %% "scalacheck"           % "1.11.3"  % "test" withSources(),
    "org.scalatest"       %%  "scalatest"           % "2.1.2"   % "test" withSources()
  )

}
