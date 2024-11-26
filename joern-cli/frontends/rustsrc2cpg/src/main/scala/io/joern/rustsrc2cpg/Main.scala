package io.joern.rustsrc2cpg

import better.files.File
import io.joern.x2cpg.X2CpgConfig
import io.joern.x2cpg.X2CpgMain
import scopt.OParser
import java.nio.file.Paths

final case class Config(rustParserPath: String = "") extends X2CpgConfig[Config] {
  def withRustParserPath(rustParserPath: String): Config = {
    this.copy(rustParserPath = rustParserPath).withInheritedFields(this)
  }
}

private object Frontend {
  implicit val defaultConfig: Config = Config()

  val cmdLineParser: OParser[Unit, Config] = {
    val builder = OParser.builder[Config]
    import builder._
    OParser.sequence(
      programName(classOf[RustCpg].getSimpleName),
      opt[String]("rust-parser-path")
        .text("""--rust-parser-path is the path to rust parser binary
        | --rust-parser-path must be an absolute path to an exist-executable file""".stripMargin)
        .required()
        .validate(x =>
          val xFile = File(x)
          if (
            xFile.exists
            && xFile.isRegularFile
            && xFile.isExecutable
            && Paths.get(x).isAbsolute
          ) success
          else failure("--rust-parser-path must be an absolute path to an exist-executable file")
        )
        .action((x, c) => c.withRustParserPath(x))
    )
  }
}

object Main extends X2CpgMain(Frontend.cmdLineParser, new RustCpg())(Frontend.defaultConfig) {
  def run(config: Config, rustcpg: RustCpg): Unit = {
    val inputDir = File(config.inputPath)
    if (inputDir.isDirectory) {
      if (inputDir.name.endsWith("projects")) {
        inputDir.list.filter(_.isDirectory).foreach { directory =>
          config.inputPath = directory.pathAsString
          var testRustCpg = RustCpg()
          testRustCpg.run(config)
        }
      } else {
        rustcpg.run(config)
      }
    } else if (inputDir.isRegularFile) {
      rustcpg.run(config)
    }
  }
}
