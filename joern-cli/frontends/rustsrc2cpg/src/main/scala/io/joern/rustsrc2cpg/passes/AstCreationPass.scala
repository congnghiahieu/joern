package io.joern.rustsrc2cpg.passes;

import com.fasterxml.jackson.databind.module.SimpleModule
import io.joern.rustsrc2cpg.Config
import io.joern.rustsrc2cpg.ast.CargoCrate
import io.joern.rustsrc2cpg.ast.FileAst
import io.joern.rustsrc2cpg.astcreation.AstCreator
import io.joern.rustsrc2cpg.parser.JsonParser
import io.joern.x2cpg.Ast
import io.joern.x2cpg.SourceFiles
import io.joern.x2cpg.utils.{ExternalCommand, Report, TimeUtils}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.passes.ForkJoinParallelCpgPass
import io.shiftleft.utils.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.util.matching.Regex

class AstCreationPass(
  cpg: Cpg,
  config: Config,
  outputDirPath: Path,
  cargoCrate: CargoCrate,
  report: Report = new Report()
) extends ForkJoinParallelCpgPass[Array[String]](cpg) {

  private val inputRootPath                        = config.inputPath
  private val sourceFileExtension: Set[String]     = Set(".rs")
  private val DefaultIgnoredFolders: List[Regex]   = List()
  private val jsonParser: JsonParser               = new JsonParser()
  private val usedPrimitiveTypes: util.Set[String] = new util.HashSet[String]()
  private val logger: Logger                       = LoggerFactory.getLogger(classOf[AstCreator])

  override def generateParts(): Array[Array[String]] = {

    val cwd        = Paths.get(".").toAbsolutePath()
    val binary     = cwd.resolve("bin/rust-parser/rust-parser")
    val binaryPath = "/home/hieucien/Workspace/joern/joern-cli/frontends/rustsrc2cpg/bin/rust-parser/rust-parser"
    val command =
      s"$binaryPath --input ${inputRootPath} --output ${outputDirPath.toString} --stdout --json --cargo-toml"

    runBinary(command) match {
      case Success(output) =>
        val outputDir                  = outputDirPath.toFile
        val traverse: ListBuffer[File] = ListBuffer(outputDir)
        val collect: ListBuffer[File]  = ListBuffer()
        while (traverse.nonEmpty) {
          val current = traverse.head
          if (current.isDirectory) {
            traverse.addAll(current.listFiles())
          } else if (current.isFile && current.getName.endsWith(".json")) {
            collect.addOne(current)
          }
          traverse.remove(0)
        }

        // logger.info(s"[generateParts] [${inputRootPath.split("/").last}] collect.length: ${collect.length}")

        val arr = collect.map(file => file.getAbsolutePath).toArray(classTag[String])
        Seq(arr).toArray
      case Failure(exception) =>
        logger.info(s"ExternalCommand run failed: ${exception.getMessage}")
        Array()
    }
  }

  override def runOnPart(builder: DiffGraphBuilder, filenames: Array[String]): Unit = {
    var cargoFileNumber = 0;
    var rustFileNumber  = 0;

    filenames.foreach(filename => {
      logger.warn("runOnPart: {}", filename)

      if (!filename.endsWith("Cargo.json")) {
        rustFileNumber += 1;
        val (gotCpg, duration) = TimeUtils.time {
          val parsedFileAst = jsonParser.parse(filename)

          val filePathAbsoluateToCrate =
            Paths
              .get(filename)
              .toAbsolutePath
              .toString
              .replaceFirst(outputDirPath.toString, inputRootPath.toString)
              .replaceFirst(".json", ".rs")
          val filePathRelativeToCrate = SourceFiles.toRelativePath(filePathAbsoluateToCrate.toString, inputRootPath)

          val localDiff =
            new AstCreator(parsedFileAst, filePathRelativeToCrate, cargoCrate, usedPrimitiveTypes)(
              config.schemaValidation
            )
              .createAst()
          builder.absorb(localDiff)
        }
      } else {
        cargoFileNumber += 1;
      }
    })

    logger.info(s"[runOnPart] [${config.inputPath.split("/").last}] fileNames.length: ${filenames.length}")
    logger.info(s"[runOnPart] [${config.inputPath.split("/").last}] cargoFileNumber: ${cargoFileNumber}")
    logger.info(s"[runOnPart] [${config.inputPath.split("/").last}] rustFileNumber: ${rustFileNumber}")
  }

  def getUsedPrimitiveTypes() = usedPrimitiveTypes

  def runBinary(command: String): Try[Seq[String]] = {
    val stdOutOutput  = new ConcurrentLinkedQueue[String]
    val stdErrOutput  = new ConcurrentLinkedQueue[String]
    val processLogger = ProcessLogger(stdOutOutput.add, stdErrOutput.add)
    val process       = Process(command, File("."))
    Try(process.!(processLogger)) match {
      case Success(0) =>
        Success(stdOutOutput.asScala.toSeq)
      case _ =>
        val allOutput = stdOutOutput.asScala.toSeq ++ stdErrOutput.asScala.toSeq
        Failure(new RuntimeException(allOutput.mkString(System.lineSeparator())))
    }
  }
}
