package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.joern.x2cpg.utils.NodeBuilders.newThisParameterNode
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer
trait AstForGenerics(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForGenerics(filename: String, parentFullname: String, generics: Generics): Ast = {
    val childrenAstList = ListBuffer[Ast]()

    if (generics.params.isDefined) {
      val genericParamsAsts = generics.params.get
        .map(astForGenericParam(filename, parentFullname, _))
        .toList
      childrenAstList.addAll(genericParamsAsts)
    }

    if (generics.whereClause.isDefined) {
      val wherePredicatesAsts = generics.whereClause.get
        .map(astForWherePredicate(filename, parentFullname, _))
        .toList
      childrenAstList.addAll(wherePredicatesAsts)
    }

    if (childrenAstList.nonEmpty) {
      var genericsNode = Ast(
        unknownNode(WrapperAst(), "")
          .parserTypeName(classOf[Generics].getSimpleName)
      )
      genericsNode.withChildren(childrenAstList.toSeq)
    } else {
      Ast()
    }
  }
}
