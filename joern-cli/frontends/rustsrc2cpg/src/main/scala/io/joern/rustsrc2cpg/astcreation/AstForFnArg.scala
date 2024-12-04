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
import io.shiftleft.codepropertygraph.generated.EdgeTypes

import scala.collection.mutable.ListBuffer

trait AstForFnArg(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>

  def astForFnArg(filename: String, parentFullname: String, fnArg: FnArg, parameterIndex: Int): Ast = {
    if (fnArg.receiverFnArg.isDefined) {
      return astForReceiver(filename, parentFullname, fnArg.receiverFnArg.get, parameterIndex)
    } else if (fnArg.typedFnArg.isDefined) {
      return astForFnArgPatType(filename, parentFullname, fnArg.typedFnArg.get, parameterIndex)
    } else {
      throw new RuntimeException(s"Unknown fnArg type: $fnArg")
    }
  }

  def astForFnArgPatType(
    filename: String,
    parentFullname: String,
    patTypeInstance: PatType,
    parameterIndex: Int
  ): Ast = {
    val code                    = codeForPatType(filename, parentFullname, patTypeInstance)
    val (lhsCode, typeFullname) = extractCodeForPatType(code)
    // remove subPat, mut and ref (see class PatIdent)
    val identOnly = lhsCode.split("@").head.replace("mut", "").replace("ref", "").trim
    val evaluationStrategy = typeFullname.contains("&") match {
      case true  => EvaluationStrategies.BY_REFERENCE
      case false => EvaluationStrategies.BY_VALUE
    }
    val letNode = localNode(patTypeInstance, identOnly, code, typeFullname)
    scope.addToScope(identOnly, (letNode, code))

    val annotationsAst = patTypeInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val patAst = patTypeInstance.pat match {
      case Some(pat) => astForPat(filename, parentFullname, pat)
      case None      => Ast()
    }
    val typeAst = patTypeInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    val parameterNode =
      parameterInNode(patTypeInstance, identOnly, code, parameterIndex, false, evaluationStrategy, typeFullname)

    Ast(parameterNode)
      .withChild(Ast(letNode))
      .withChild(patAst)
      .withChild(typeAst)
      .withChildren(annotationsAst)
  }

  def astForReceiver(filename: String, parentFullname: String, receiverInstance: Receiver, parameterIndex: Int): Ast = {
    val evaluationStrategy = receiverInstance.ref match {
      case Some(true) => EvaluationStrategies.BY_REFERENCE
      case _          => EvaluationStrategies.BY_VALUE
    }
    val name = "self"
    val typeFullname = receiverInstance.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    var codePrefix = receiverInstance.ref match {
      case Some(true) => s"&"
      case _          => s""
    }
    codePrefix = receiverInstance.lifetime match {
      case Some(lifetime) => s"${codePrefix}${codeForLifetime(filename, parentFullname, lifetime)}"
      case _              => codePrefix
    }
    codePrefix = receiverInstance.mut match {
      case Some(true) => s"${codePrefix} mut"
      case _          => codePrefix
    }
    val code = s"${codePrefix} self"

    val parameterNode =
      newThisParameterNode(name, code, typeFullname, evaluationStrategy = evaluationStrategy).index(parameterIndex)
    scope.addToScope(name, (parameterNode, typeFullname))

    val annotationsAst = receiverInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val typeAst = receiverInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val lifetimeAst = receiverInstance.lifetime match {
      case Some(lifetime) => {
        val ast = astForLifetime(filename, parentFullname, lifetime)
        ast.root.get match {
          case lifetimeNode: NewLifetime => {
            diffGraph.addEdge(parameterNode, lifetimeNode, EdgeTypes.AST)
            // diffGraph.addEdge(parameterNode, lifetimeNode, EdgeTypes.OUT_LIVE)
            // diffGraph.addEdge(parameterNode, lifetimeNode, EdgeTypes.REF)
          }
          case _ => {
            throw new RuntimeException("Unexpected node type")
          }
        }
        ast
      }
      case None => Ast()
    }

    Ast(parameterNode)
      .withChild(typeAst)
      // .withChild(lifetimeAst)
      .withChildren(annotationsAst)
  }

  def astForBareFnArg(filename: String, parentFullname: String, bareFnArgInstance: BareFnArg): Ast = {
    val annotationsAst = bareFnArgInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val code = codeForBareFnArg(filename, parentFullname, bareFnArgInstance)
    val node = typeRefNode(bareFnArgInstance, code, code)

    Ast(node)
    // .withChildren(annotationsAst)
  }

  def codeForBareFnArg(filename: String, parentFullname: String, bareFnArgInstance: BareFnArg): String = {
    var code = bareFnArgInstance.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    code
  }

  def astForBareVariadic(filename: String, parentFullname: String, bareVariadicInstance: BareVariadic): Ast = {
    val annotationsAst = bareVariadicInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val code = codeForBareVariadic(filename, parentFullname, bareVariadicInstance)
    val node = parameterInNode(bareVariadicInstance, "", code, 0, false, EvaluationStrategies.BY_VALUE, "")
    scope.addToScope(code, (node, code))

    Ast(node).withChildren(annotationsAst)
  }

  def codeForBareVariadic(filename: String, parentFullname: String, bareVariadicInstance: BareVariadic): String = {
    // Implement the logic to generate the code string for BareVariadic
    val code = bareVariadicInstance.name match {
      case Some(name) => s"${name}: ..."
      case None       => "..."
    }
    code
  }
}
