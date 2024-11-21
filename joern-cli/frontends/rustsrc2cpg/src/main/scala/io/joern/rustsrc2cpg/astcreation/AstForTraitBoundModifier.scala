package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer

trait AstForTraitBoundModifier(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForTraitBound(filename: String, parentFullname: String, traitBound: TraitBound): Ast = {

    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val pathAst = traitBound.path match {
      case Some(path) => astForPath(filename, parentFullname, path)
      case None       => Ast()
    }

    val lifetimeBoundsAst = traitBound.lifetimes match {
      case Some(lifetimes) => lifetimes.map(astForGenericParam(filename, parentFullname, _)).toList
      case None            => List()
    }

    lifetimeBoundsAst.isEmpty match {
      case true => pathAst
      case false => {
        val code = codeForTraitBound(filename, parentFullname, traitBound)

        Ast(unknownNode(traitBound, code))
          .withChild(pathAst)
          .withChildren(lifetimeBoundsAst)
      }
    }
  }

  def codeForTraitBound(filename: String, parentFullname: String, traitBound: TraitBound): String = {
    val typeFullname = traitBound.path match {
      case Some(path) => typeFullnameForPath(filename, parentFullname, path)
      case None       => Defines.Unknown
    }

    var code = traitBound.paren_token match {
      case Some(true) => s"($typeFullname)"
      case _          => typeFullname
    }
    code = traitBound.modifier match {
      case Some(TraitBoundModifier.Maybe) => s"?$code"
      case _                              => code
    }
    code = traitBound.lifetimes match {
      case Some(lifetimes) => {
        val lifetimeParameterCode = lifetimes.map(codeForGenericParam(filename, parentFullname, _)).mkString(", ")
        s"for <$lifetimeParameterCode> $code"
      }
      case None => code
    }

    code
  }
}
