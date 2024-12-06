package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.Defines.Unknown
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.joern.x2cpg.utils.NodeBuilders.newThisParameterNode
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer

trait AstForFn(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>

  def astForReturnType(filename: String, parentFullname: String, returnTypeInstance: ReturnType): Ast = {
    if (!returnTypeInstance.isDefined) {
      return Ast(unknownNode(UnknownAst(), "").parserTypeName(classOf[ReturnType].getSimpleName))
    }

    return astForType(filename, parentFullname, returnTypeInstance.get)
  }

  def astForVariadic(filename: String, parentFullname: String, variadicInstance: Variadic): Ast = {
    val annotationsAst = variadicInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val name = variadicInstance.pat match {
      case Some(pat) => codeForPat(filename, parentFullname, pat)
      case None      => Defines.Unknown
    }
    val code = variadicInstance.comma match {
      case Some(true) => s"$name: ...,"
      case _          => s"$name: ..."
    }

    val node = parameterInNode(variadicInstance, name, code, -1, true, EvaluationStrategies.BY_VALUE, "")
    scope.addToScope(name, (node, code))

    Ast(node)
      .withChildren(annotationsAst)
  }

  def astForVariant(filename: String, parentFullname: String, variantInstance: Variant): Ast = {
    val name = variantInstance.ident
    val code = variantInstance.discriminant match {
      case Some(discriminant) => s"$name = ${codeForExpr(filename, parentFullname, discriminant)}"
      case None               => name
    }
    val typeFullname = s"${parentFullname}::${variantInstance.ident}"

    val annotationsAst = variantInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, typeFullname, _)).toList
      case None        => List()
    }

    val node = memberNode(variantInstance, name, code, typeFullname)
      .astParentFullName("Variant")
      .astParentType("Variant")

    val parentScope = scope.popScope()
    // Variant is a also a type, so we need to add it to the parent scope
    scope.addToScope(typeFullname, (node, typeFullname))
    parentScope.foreach(scope.pushNewScope)

    variantInstance.discriminant match {
      case Some(discriminant) => {
        val disciminantAst = astForExpr(filename, typeFullname, discriminant)
        Ast(node)
          .withChild(disciminantAst)
          .withChildren(annotationsAst)
      }
      case None => {

        scope.pushNewScope(node)

        val fieldsAst = variantInstance.fields match {
          case Some(fields) => astForFields(filename, typeFullname, fields)
          case None         => List()
        }

        scope.popScope()

        Ast(node)
          .withChildren(fieldsAst)
          .withChildren(annotationsAst)
      }
    }
  }
}
