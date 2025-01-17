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
import io.joern.x2cpg.utils.NodeBuilders.newOperatorCallNode
import io.shiftleft.codepropertygraph.generated.Operators
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
    scope.addToScope(constImplItemInstance.ident, (constNode, typeFullName))

    val annotationsAst = constImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, constImplItemInstance.vis)
    val identAst     = astForIdent(filename, parentFullname, constImplItemInstance.ident)
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

    val assignmentNode = newOperatorCallNode(Operators.assignment, fullCode)

    callAst(assignmentNode, Seq(identAst, typeAst, exprAst))
      .withChild(Ast(constNode))
      .withChild(genericAst)
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForImplItemFn(filename: String, parentFullname: String, fnImplItemInstance: ImplItemFn): Ast = {
    val newMethodNode = methodNode(fnImplItemInstance, fnImplItemInstance.ident, fnImplItemInstance.ident, "", filename)
    scope.addToScope(fnImplItemInstance.ident, (newMethodNode, fnImplItemInstance.ident))

    scope.pushNewScope(newMethodNode)

    val annotationsAst = fnImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, fnImplItemInstance.vis)
    val genericsAst = fnImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val parameterIns = fnImplItemInstance.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
    val variadicAst = fnImplItemInstance.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
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
    val bodyAst = astForBlock(filename, parentFullname, fnImplItemInstance.stmts)

    scope.popScope()

    val methodAst =
      Ast(newMethodNode)
        .withChildren(parameterIns :+ variadicAst)
        .withChild(bodyAst)
        // .withChild(Ast(modifierNode))
        .withChild(methodRetNode)
        .withChildren(annotationsAst)
        .withChild(genericsAst)

    methodAst

    // val node = memberNode(fnImplItemInstance, fnImplItemInstance.ident, "", "")
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(methodAst)
  }

  def astForImplItemType(filename: String, parentFullname: String, typeImplItemInstance: ImplItemType): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, typeImplItemInstance.vis)

    var (declNode, code) = typeImplItemInstance.ty match {
      case Some(ty) => {
        val typeFullname = typeFullnameForType(filename, parentFullname, ty)
        val code         = s"type ${typeImplItemInstance.ident} = ${typeFullname}"
        val node =
          typeDeclNode(typeImplItemInstance, typeImplItemInstance.ident, typeImplItemInstance.ident, filename, code)
            .aliasTypeFullName(typeFullname)
        (node, code)
      }
      case None => {
        val code = s"type ${typeImplItemInstance.ident}"
        val node =
          typeDeclNode(typeImplItemInstance, typeImplItemInstance.ident, typeImplItemInstance.ident, filename, code)
        (node, code)
      }
    }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    scope.addToScope(typeImplItemInstance.ident, (declNode, code))

    scope.pushNewScope(declNode)

    val annotationsAst = typeImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = typeImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val typeAst = typeImplItemInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    scope.popScope()

    Ast(declNode)
      .withChild(typeAst)
      .withChild(genericsAst)
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)

    // val node = memberNode(typeImplItemInstance, typeImplItemInstance.ident, code, typeImplItemInstance.ident)
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(Ast(declNode))
    //   .withChild(typeAst)
    //   .withChild(genericsAst)
    //   // .withChild(Ast(modifierNode))
    //   .withChildren(annotationsAst)
  }

  def astForImplItemMacro(filename: String, parentFullname: String, macroImplItemInstance: ImplItemMacro): Ast = {
    val annotationsAst = macroImplItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val marcoInstance =
      Macro(macroImplItemInstance.path, macroImplItemInstance.delimiter, macroImplItemInstance.tokens)
    val macroAst = astForMacro(filename, parentFullname, marcoInstance, macroImplItemInstance.semi_token)
      .withChildren(annotationsAst)

    macroAst

    // val node = memberNode(macroImplItemInstance, "", "", "")
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(macroAst)
  }
}
