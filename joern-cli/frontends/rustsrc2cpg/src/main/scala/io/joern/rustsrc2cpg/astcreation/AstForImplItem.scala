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
trait AstForImplItem(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForImplItem(filename: String, parentFullname: String, implItemInstance: ImplItem): Ast = {
    if (implItemInstance.constImplItem.isDefined) {
      astForImplItemConst(filename, parentFullname, implItemInstance.constImplItem.get)
    } else if (implItemInstance.fnImplItem.isDefined) {
      astForImplItemFn(filename, parentFullname, implItemInstance.fnImplItem.get)
    } else if (implItemInstance.typeImplItem.isDefined) {
      astForImplItemType(filename, parentFullname, implItemInstance.typeImplItem.get)
    } else if (implItemInstance.macroImplItem.isDefined) {
      astForImplItemMacro(filename, parentFullname, implItemInstance.macroImplItem.get)
    } else if (implItemInstance.verbatimImplItem.isDefined) {
      astForTokenStream(filename, parentFullname, implItemInstance.verbatimImplItem.get)
    } else {
      throw new IllegalArgumentException("Unsupported impl item type")
    }
  }

  def astForImplItemConst(filename: String, parentFullname: String, constImplItemInstance: ImplItemConst): Ast = {
    val annotationsAst = constImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, constImplItemInstance.vis)
    val typeAst = constImplItemInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val exprAst = constImplItemInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val genericAst = constImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    val typeFullName = constImplItemInstance.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    val exprCode = constImplItemInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val localCode = s"const ${constImplItemInstance.ident}: ${typeFullName}"
    val fullCode  = s"${localCode} = ${exprCode}"

    val constNode = localNode(constImplItemInstance, constImplItemInstance.ident, localCode, typeFullName)
    localNodeMap.put(constImplItemInstance.ident, constNode)

    Ast(memberNode(constImplItemInstance, constImplItemInstance.ident, fullCode, typeFullName))
      .withChild(Ast(constNode))
      .withChild(typeAst)
      .withChild(exprAst)
      .withChild(genericAst)
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForImplItemFn(filename: String, parentFullname: String, fnImplItemInstance: ImplItemFn): Ast = {
    val newMethodNode = methodNode(fnImplItemInstance, fnImplItemInstance.ident, "", "", filename).isExternal(
      fnImplItemInstance.stmts.isEmpty
    )

    val annotationsAst = fnImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val bodyAst = astForBlock(filename, parentFullname, fnImplItemInstance.stmts)
    val parameterIns = fnImplItemInstance.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
    val methodRetNode = fnImplItemInstance.output match {
      case Some(output) => {
        val typeFullname = typeFullnameForType(filename, parentFullname, output)
        val typeAst      = astForType(filename, parentFullname, output)

        Ast(
          methodReturnNode(UnknownAst(), typeFullname)
            .code(typeFullname)
        )
          .withChild(typeAst)
      }
      case None => Ast(methodReturnNode(UnknownAst(), ""))
    }
    val variadicAst = fnImplItemInstance.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
    val genericsAst = fnImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    val modifierNode = modifierForVisibility(filename, parentFullname, fnImplItemInstance.vis)

    val methodAst =
      Ast(newMethodNode)
        .withChildren(parameterIns :+ variadicAst)
        .withChild(bodyAst)
        .withChild(Ast(modifierNode))
        .withChild(methodRetNode)
        .withChildren(annotationsAst)
        .withChild(genericsAst)

    Ast(memberNode(fnImplItemInstance, fnImplItemInstance.ident, "", ""))
      .withChild(methodAst)
  }

  def astForImplItemType(filename: String, parentFullname: String, typeImplItemInstance: ImplItemType): Ast = {
    val annotationsAst = typeImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, typeImplItemInstance.vis)
    val genericsAst = typeImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val typeAst = typeImplItemInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    val typeFullname = typeImplItemInstance.ty.map(typeFullnameForType(filename, parentFullname, _)).getOrElse("")
    val code         = s"type ${typeImplItemInstance.ident} = ${typeFullname}"
    val node =
      typeDeclNode(typeImplItemInstance, typeImplItemInstance.ident, typeFullname, filename, code)

    Ast(memberNode(typeImplItemInstance, typeImplItemInstance.ident, code, typeFullname))
      .withChild(Ast(node))
      .withChild(typeAst)
      .withChild(genericsAst)
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForImplItemMacro(filename: String, parentFullname: String, macroImplItemInstance: ImplItemMacro): Ast = {
    val annotationsAst = macroImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val marcoInstance =
      Macro(macroImplItemInstance.path, macroImplItemInstance.delimiter, macroImplItemInstance.tokens)
    val macroAst = astForMacro(filename, parentFullname, marcoInstance).withChildren(annotationsAst)

    Ast(memberNode(macroImplItemInstance, "", "", ""))
      .withChild(macroAst)
  }
}
