import com.typesafe.config.{Config, ConfigFactory}
import versionsort.VersionHelper

import scala.sys.process.stringToProcess
import scala.util.Try

name := "rustsrc2cpg"

dependsOn(Projects.dataflowengineoss % "compile->compile;test->test", Projects.x2cpg % "compile->compile;test->test")

libraryDependencies ++= Seq(
  "io.shiftleft"  %% "codepropertygraph" % Versions.cpg,
  "org.scalatest" %% "scalatest"         % Versions.scalatest % Test,
  "com.lihaoyi"   %% "os-lib"            % "0.9.1",
  // Custom dependencies
  "com.fasterxml.jackson.core"    % "jackson-databind"     % "2.15.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
  "com.google.code.gson"          % "gson"                 % "2.10.1",
  "org.apache.commons"            % "commons-compress"     % "1.26.1"
)

scalacOptions ++= Seq(
  "-deprecation" // Emit warning and location for usages of deprecated APIs.
)

enablePlugins(JavaAppPackaging, LauncherJarPlugin)

lazy val appProperties = settingKey[Config]("App Properties")
appProperties := {
  val path            = (Compile / resourceDirectory).value / "application.conf"
  val applicationConf = ConfigFactory.parseFile(path).resolve()
  applicationConf
}

lazy val RustParser = "rust-parser"
