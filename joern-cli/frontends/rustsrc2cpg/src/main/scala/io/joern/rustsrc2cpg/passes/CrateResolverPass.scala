package io.joern.rustsrc2cpg.passes

import io.joern.rustsrc2cpg.ast.CargoCrate
import io.joern.x2cpg.Defines
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.codepropertygraph.generated.DiffGraphBuilder
import io.shiftleft.semanticcpg.language.types.structure.NamespaceTraversal
import io.shiftleft.semanticcpg.language.types.structure.NamespaceTraversal.globalNamespaceName

class CrateResolverPass(cpg: Cpg, cargoCrate: CargoCrate) extends CpgPass(cpg) {

  override def run(builder: DiffGraphBuilder): Unit = {
    val crateNamespace = NewNamespaceBlock()
      .name(NamespaceTraversal.globalNamespaceName)
      .filename(cargoCrate.crateName)
      .fullName(cargoCrate.cratePath)
    builder.addNode(crateNamespace)

    // cpg.file.foreach(file => {
    //   if (!file.name.equals(NamespaceTraversal.globalNamespaceName)) {
    //     builder.addEdge(crateNamespace, file, EdgeTypes.AST)
    //   }
    // })
    cpg.namespaceBlock.foreach(namespaceBlock => {
      if (!namespaceBlock.name.equals(NamespaceTraversal.globalNamespaceName)) {
        builder.addEdge(crateNamespace, namespaceBlock, EdgeTypes.AST)
      }
    })
  }
}
