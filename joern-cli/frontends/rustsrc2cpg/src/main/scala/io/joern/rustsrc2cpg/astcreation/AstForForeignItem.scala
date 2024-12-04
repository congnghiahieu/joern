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
import io.shiftleft.codepropertygraph.generated.Operators
import io.joern.x2cpg.utils.NodeBuilders.newOperatorCallNode

trait AstForForeignItem(implicit schemaValidationMode: ValidationMode) {
  this: AstCreator =>
  def astForForeignItem(filename: String, parentFullname: String, foreignItemInstance: ForeignItem): Ast = {
    if (foreignItemInstance.fnForeignItem.isDefined) {
      astForForeignItemFn(filename, parentFullname, foreignItemInstance.fnForeignItem.get)
    } else if (foreignItemInstance.staticForeignItem.isDefined) {
      astForForeignItemStatic(filename, parentFullname, foreignItemInstance.staticForeignItem.get)
    } else if (foreignItemInstance.typeForeignItem.isDefined) {
      astForForeignItemType(filename, parentFullname, foreignItemInstance.typeForeignItem.get)
    } else if (foreignItemInstance.macroForeignItem.isDefined) {
      astForForeignItemMacro(filename, parentFullname, foreignItemInstance.macroForeignItem.get)
    } else if (foreignItemInstance.verbatimForeignItem.isDefined) {
      astForTokenStream(filename, parentFullname, foreignItemInstance.verbatimForeignItem.get)
    } else {
      throw new IllegalArgumentException("Unsupported foreign item type")
    }
  }

  def astForForeignItemFn(filename: String, parentFullname: String, fnForeignItemInstance: ForeignItemFn): Ast = {
    val newMethodNode =
      methodNode(fnForeignItemInstance, fnForeignItemInstance.ident, fnForeignItemInstance.ident, "", filename)
        .isExternal(true)
    scope.addToScope(fnForeignItemInstance.ident, (newMethodNode, fnForeignItemInstance.ident))

    scope.pushNewScope(newMethodNode)

    val annotationsAst = fnForeignItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, fnForeignItemInstance.vis)
    val genericsAst = fnForeignItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val parameterIns = fnForeignItemInstance.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
    val variadicAst = fnForeignItemInstance.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
    val methodRetNode = fnForeignItemInstance.output match {
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

    scope.popScope()

    Ast(newMethodNode)
      .withChildren(parameterIns :+ variadicAst)
      // .withChild(Ast(modifierNode))
      .withChild(methodRetNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
  }

  def astForForeignItemStatic(
    filename: String,
    parentFullname: String,
    staticForeignItemInstance: ForeignItemStatic
  ): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, staticForeignItemInstance.vis)

    val typeFullname = staticForeignItemInstance.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    val isMut = staticForeignItemInstance.mut.contains(StaticMutability.Mut)
    var code = if (isMut) { s"static mut ${staticForeignItemInstance.ident}: ${typeFullname}" }
    else { s"static ${staticForeignItemInstance.ident}: ${typeFullname}" }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val staticNode = localNode(staticForeignItemInstance, staticForeignItemInstance.ident, code, typeFullname)
    scope.addToScope(staticForeignItemInstance.ident, (staticNode, typeFullname))

    val annotationsAst = staticForeignItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val identAst = astForIdent(filename, parentFullname, staticForeignItemInstance.ident)
    val typeAst = staticForeignItemInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    val assignmentNode = newOperatorCallNode(Operators.assignment, code)

    callAst(assignmentNode, Seq(identAst, typeAst))
      .withChild(Ast(staticNode))
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForForeignItemType(filename: String, parentFullname: String, typeForeignItemInstance: ForeignItemType): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, typeForeignItemInstance.vis)

    var code = s"type ${typeForeignItemInstance.ident}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val newItemTypeNode =
      typeDeclNode(
        typeForeignItemInstance,
        typeForeignItemInstance.ident,
        typeForeignItemInstance.ident,
        filename,
        code
      )
    scope.addToScope(typeForeignItemInstance.ident, (newItemTypeNode, code))

    scope.pushNewScope(newItemTypeNode)

    val annotationsAst = typeForeignItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst =
      typeForeignItemInstance.generics match {
        case Some(generics) => astForGenerics(filename, parentFullname, generics)
        case None           => Ast()
      }

    scope.popScope()

    Ast(newItemTypeNode)
      .withChild(genericsAst)
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForForeignItemMacro(
    filename: String,
    parentFullname: String,
    macroForeignItemInstance: ForeignItemMacro
  ): Ast = {
    val annotationsAst = macroForeignItemInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val macroRustAst =
      Macro(macroForeignItemInstance.path, macroForeignItemInstance.delimiter, macroForeignItemInstance.tokens)
    astForMacro(filename, parentFullname, macroRustAst, macroForeignItemInstance.semi_token)
      .withChildren(annotationsAst)
  }
}
