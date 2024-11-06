package io.joern.console.cpgcreation

import io.joern.console.FrontendConfig

import java.nio.file.Path
import scala.util.Try

/** Language frontend for Go code. Translates Go source code into Code Property Graphs.
  */
case class RustCpgGenerator(config: FrontendConfig, rootPath: Path) extends CpgGenerator {
  private lazy val rustSrc2CpgCommand: Path =
    if (isWin) rootPath.resolve("rustsrc2cpg.bat") else rootPath.resolve("rustsrc2cpg")

  /** Generate a CPG for the given input path. Returns the output path, or None, if no CPG was generated.
    */
  override def generate(inputPath: String, outputPath: String): Try[String] = {
    if (rust2CpgAvailable()) rust2CpgGenerate(inputPath, outputPath) else rustSrc2CpgGenerate(inputPath, outputPath)
  }

  def rust2CpgGenerate(inputPath: String, outputPath: String = "cpg.bin.zip"): Try[String] = {
    var command   = rootPath.resolve("rust2cpg.sh").toString
    var arguments = Seq("--output", outputPath) ++ config.cmdLineParams ++ Seq("generate") ++ List(inputPath)

    if (System.getProperty("os.name").startsWith("Windows")) {
      command = "powershell"
      arguments = Seq(rootPath.resolve("rust2cpg.ps1").toString) ++ arguments
    }

    runShellCommand(command, arguments).map(_ => outputPath)
  }

  def rustSrc2CpgGenerate(inputPath: String, outputPath: String): Try[String] = {
    val arguments = List(inputPath) ++ Seq("-o", outputPath) ++ config.cmdLineParams
    runShellCommand(rustSrc2CpgCommand.toString, arguments).map(_ => outputPath)
  }

  def rust2CpgAvailable(): Boolean = {
    if (isWin) rootPath.resolve("rust2cpg.ps1").toFile.exists() else rootPath.resolve("rust2cpg.sh").toFile.exists()
  }

  def rustSrc2CpgAvailable(): Boolean = {
    rustSrc2CpgCommand.toFile.exists
  }

  override def isAvailable: Boolean = rust2CpgAvailable() || rustSrc2CpgAvailable()

  override def isJvmBased = false
}
