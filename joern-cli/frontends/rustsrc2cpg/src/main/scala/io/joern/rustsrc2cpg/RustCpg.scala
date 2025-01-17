package io.joern.rustsrc2cpg

import io.joern.rustsrc2cpg.Config
import io.joern.rustsrc2cpg.Frontend.defaultConfig
import io.joern.rustsrc2cpg.ast.*
import io.joern.rustsrc2cpg.passes.AstCreationPass
import io.joern.rustsrc2cpg.passes.CrateResolverPass
import io.joern.rustsrc2cpg.passes.TypeResolverPass
import io.joern.x2cpg.X2Cpg
import io.joern.x2cpg.X2CpgFrontend
import io.joern.x2cpg.passes.frontend.MetaDataPass
import io.joern.x2cpg.utils.Report
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import better.files.File.{usingTemporaryDirectory, newTemporaryDirectory}

import java.io.File
import scala.util.Try

class RustCpg extends X2CpgFrontend[Config] {

  private val report: Report = new Report()

  override def createCpg(config: Config): Try[Cpg] = {
    val rootPath = config.inputPath
    val rootFile = File(rootPath)
    if (!rootFile.isDirectory && !rootFile.isFile) {
      throw new IllegalArgumentException(s"${rootFile.toString()} is not a valid directory or file.")
    }

    X2Cpg.withNewEmptyCpg(config.outputPath, config) { (cpg, config) =>
      val tempOutputDir = newTemporaryDirectory("rustsrc2cpg_")
      val cargoCrate    = CargoCrate(config)

      new MetaDataPass(cpg, Languages.RUSTLANG, rootPath).createAndApply()

      val astCreationPass = new AstCreationPass(cpg, config, tempOutputDir.path, cargoCrate, report)
      astCreationPass.createAndApply()

      // val typeResolverPass =
      //   new TypeResolverPass(cpg, astCreationPass.getUsedPrimitiveTypes().toSeq)
      // typeResolverPass.createAndApply()

      val moduleResovelerPass = new CrateResolverPass(cpg, cargoCrate)
      moduleResovelerPass.createAndApply()

      report.print()
    }
  }
}
