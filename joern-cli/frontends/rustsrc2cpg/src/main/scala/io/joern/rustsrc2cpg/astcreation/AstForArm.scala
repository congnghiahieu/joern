package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.joern.x2cpg.utils.NodeBuilders.newThisParameterNode
import io.shiftleft.codepropertygraph.generated.ControlStructureTypes
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer

trait AstForArm(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForArm(filename: String, parentFullname: String, arm: Arm): Ast = {
    val annotationsAst = arm.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val code    = "case"
    val armNode = controlStructureNode(arm, "CASE", code)

    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val patAst = arm.pat match {
      case Some(pat) => astForPat(filename, armNode.parserTypeName, pat)
      case None      => Ast()
    }
    val guardAst = arm.guard match {
      case Some(guard) => astForExpr(filename, armNode.parserTypeName, guard)
      case None        => Ast()
    }
    val conditionAst = Ast(unknownNode(UnknownAst(), "")).withChildren(List(patAst, guardAst))
    val bodyAst = arm.body match {
      case Some(body) => astForExpr(filename, armNode.parserTypeName, body)
      case None       => Ast()
    }

    controlStructureAst(armNode, Some(conditionAst), Seq(bodyAst))
      .withChildren(annotationsAst)
  }

  def astForLabel(filename: String, parentFullname: String, label: Label): Ast = {
    val code = s"'${label}:"
    val labelNode = jumpTargetNode(UnknownAst(), label, code)
      .parserTypeName(classOf[Label].getSimpleName())
    Ast(labelNode)
  }
}
