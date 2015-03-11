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
      libraryDependencies ++= Dependencies.clusterKernel,
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
    organizationName := "Hack Attack"
    //organizationHomepage := Some(url("http://robertcourtney.net"))
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

  val clusterKernel = Seq(
    "ch.qos.logback"      %  "logback-classic"      % "1.1.1" force,
    "com.github.nscala-time" %% "nscala-time"       % "0.8.0",
    "com.h2database"      %  "h2"                   % "1.3.170",
    "com.typesafe"        %% "scalalogging-slf4j"   % "1.1.0",
    "com.typesafe.akka"   %% "akka-actor"           % V.Akka,
    "com.typesafe.akka"   %% "akka-camel"           % V.Akka,
    "com.typesafe.akka"   %% "akka-kernel"          % V.Akka,
    "com.typesafe.akka"   %% "akka-slf4j"           % V.Akka,
    "com.typesafe.akka"   %% "akka-testkit"         % V.Akka,
    "com.typesafe.slick"  %% "slick"                % V.Slick,
    "com.typesafe.slick"  %% "slick-extensions"     % V.SlickExtensions,
    "commons-codec"       %  "commons-codec"        % "1.9",
    "commons-dbcp"        %  "commons-dbcp"         % "1.4",
    "io.spray"            %  "spray-can"            % V.Spray,
    "io.spray"            %  "spray-routing"        % V.Spray,
    "io.spray"            %  "spray-testkit"        % V.Spray,
    "io.spray"            %% "spray-json"           % V.SprayJson,
    "mysql"               %  "mysql-connector-java" % "5.1.29",
    "org.apache.camel"    %  "camel-jms"            % V.Camel withSources(),
    "org.apache.camel"    %  "camel-jetty"          % V.Camel withSources(),
    "org.apache.camel"    %  "camel-ftp"            % V.Camel withSources(),
    "org.apache.camel"    %  "camel-test"           % V.Camel withSources(),
    "org.apache.sshd"     % "sshd-core"             % "0.12.0",
    "org.codehaus.janino" % "janino"                % "2.6.1",
    "org.mockito"         % "mockito-core"          % "1.9.5" % "test",
    "org.scalacheck"      %% "scalacheck"           % "1.11.3"  % "test" withSources(),
    "org.scalatest"       %%  "scalatest"           % "2.1.2"   % "test" withSources()
  )

}
