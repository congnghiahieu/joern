package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer

trait AstForMacro(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForMacro(
    filename: String,
    parentFullname: String,
    macroInstance: Macro,
    semiToken: Option[Boolean] = None,
    ident: Option[Ident] = None
  ): Ast = {
    val (methodFullName, input, code) = codeForMacro(filename, parentFullname, macroInstance, semiToken, ident)

    val argAst = macroInstance.tokens match {
      case Some(tokens) => astForTokenStream(filename, parentFullname, tokens)
      case None         => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.METHODREF_NODE)
    val pathAst = macroInstance.path match {
      case Some(path) => astForPath(filename, parentFullname, path)
      case None       => Ast()
    }
    // Macro function is built in or some time imported but can not specify

    val callExprNode =
      callNode(macroInstance, code, methodFullName, methodFullName, DispatchTypes.INLINED)
    callAst(callExprNode, Seq(argAst))
      .withChild(pathAst)
  }

  def codeForMacro(
    filename: String,
    parentFullname: String,
    macroInstance: Macro,
    semiToken: Option[Boolean] = None,
    ident: Option[Ident] = None
  ): (String, String, String) = {
    val methodFullname = macroInstance.path match {
      case Some(path) => s"${typeFullnameForPath(filename, parentFullname, path)}!"
      case None       => Defines.Unknown
    }
    val input = macroInstance.tokens match {
      case Some(tokens) => codeForTokenStream(filename, parentFullname, tokens)
      case None         => Defines.Unknown
    }
    val delimeterCode = macroInstance.delimiter match {
      case Some(delimiter) => {
        delimiter match {
          case MacroDelimiter.Brace   => s"{$input}"
          case MacroDelimiter.Paren   => s"($input)"
          case MacroDelimiter.Bracket => s"[$input]"
        }
      }
      case None => s" $input"
    }

    var code = ident match {
      case Some(ident) => s"$methodFullname $ident $delimeterCode"
      case None        => s"$methodFullname$delimeterCode"
    }
    code = semiToken match {
      case Some(true) => s"$code;"
      case _          => code
    }

    (methodFullname, input, code)
  }
}
