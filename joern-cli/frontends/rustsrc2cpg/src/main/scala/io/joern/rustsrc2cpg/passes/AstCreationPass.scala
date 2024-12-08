package io.joern.rustsrc2cpg.passes;

import com.fasterxml.jackson.databind.module.SimpleModule
import io.joern.rustsrc2cpg.Config
import io.joern.rustsrc2cpg.ast.CargoCrate
import io.joern.rustsrc2cpg.ast.FileAst
import io.joern.rustsrc2cpg.astcreation.AstCreator
import io.joern.rustsrc2cpg.parser.JsonParser
import io.joern.x2cpg.Ast
import io.joern.x2cpg.SourceFiles
import io.joern.x2cpg.utils.ExternalCommand
import io.joern.x2cpg.utils.Report
import io.joern.x2cpg.utils.TimeUtils
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.passes.ForkJoinParallelCpgPass
import io.shiftleft.utils.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.sys.process.Process
import scala.sys.process.ProcessLogger
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex
import io.shiftleft.codepropertygraph.generated.nodes.NewConfigFile

class AstCreationPass(
  cpg: Cpg,
  config: Config,
  outputDirPath: Path,
  cargoCrate: CargoCrate,
  report: Report = new Report()
) extends ForkJoinParallelCpgPass[Array[String]](cpg) {

  private val inputRootPath                       = config.inputPath
  private val sourceFileExtension: Set[String]    = Set(".rs")
  private val DefaultIgnoredFolders: List[Regex]  = List()
  private val jsonParser: JsonParser              = new JsonParser()
  private val usedPrimitiveTypes: HashSet[String] = HashSet.empty
  private val logger: Logger                      = LoggerFactory.getLogger(classOf[AstCreator])

  override def generateParts(): Array[Array[String]] = {
    val command =
      s"${config.rustParserPath} --input ${inputRootPath} --output ${outputDirPath.toString} --stdout --stderr --json --cargo-toml"

    runShellCommand(command) match {
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

        // logger.warn(s"[generateParts] [${inputRootPath.split("/").last}] collect.length: ${collect.length}")

        val arr = collect.map(file => file.getAbsolutePath).toArray(classTag[String])
        Seq(arr).toArray
      case Failure(exception) =>
        throw new RuntimeException(s"""Rust parser run failed! Please check the your rust parser binary
        Run command ${command}""".stripMargin)
    }
  }

  override def runOnPart(builder: DiffGraphBuilder, resultFilePaths: Array[String]): Unit = {
    var cargoFileNumber = 0;
    var rustFileNumber  = 0;

    resultFilePaths.foreach(resultFilePath => {
      logger.warn("runOnPart: {}", resultFilePath)

      val filePathAbsoluateToCrate = resultFilePath.endsWith("Cargo.json") match {
        case true =>
          Paths
            .get(resultFilePath)
            .toAbsolutePath
            .toString
            .replaceFirst(outputDirPath.toString, inputRootPath.toString)
            .replaceFirst("\\.json", ".toml")
        case false =>
          Paths
            .get(resultFilePath)
            .toAbsolutePath
            .toString
            .replaceFirst(outputDirPath.toString, inputRootPath.toString)
            .replaceFirst("\\.json", ".rs")
      }
      val filePathRelativeToCrate = SourceFiles.toRelativePath(filePathAbsoluateToCrate.toString, inputRootPath)
      val fileLOC                 = io.shiftleft.utils.IOUtils.readLinesInFile(Paths.get(filePathAbsoluateToCrate)).size

      report.addReportInfo(filePathRelativeToCrate, fileLOC, parsed = true)

      val (gotCpg, duration) = TimeUtils.time {
        if (!resultFilePath.endsWith("Cargo.json")) {
          rustFileNumber += 1;

          val parsedFileAst = jsonParser.parse(resultFilePath)
          val localDiff = new AstCreator(parsedFileAst, filePathRelativeToCrate, cargoCrate, usedPrimitiveTypes)(
            config.schemaValidation
          ).createAst()
          builder.absorb(localDiff)
        } else {
          cargoFileNumber += 1;

          val fileContent = io.shiftleft.utils.IOUtils.readEntireFile(Paths.get(filePathAbsoluateToCrate))
          val node = NewConfigFile()
            .name("Cargo.toml")
            .content(fileContent)
          builder.addNode(node)
        }

        true
      }

      report.updateReport(filePathRelativeToCrate, gotCpg, duration)
    })

    logger.warn(s"[runOnPart] [${config.inputPath.split("/").last}] fileNames.length: ${resultFilePaths.length}")
    logger.warn(s"[runOnPart] [${config.inputPath.split("/").last}] cargoFileNumber: ${cargoFileNumber}")
    logger.warn(s"[runOnPart] [${config.inputPath.split("/").last}] rustFileNumber: ${rustFileNumber}")
  }

  def getUsedPrimitiveTypes() = usedPrimitiveTypes

  def runShellCommand(command: String): Try[Seq[String]] = {
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
