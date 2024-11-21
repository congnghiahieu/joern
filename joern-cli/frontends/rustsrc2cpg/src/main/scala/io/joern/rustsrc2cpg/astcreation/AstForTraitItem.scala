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

trait AstForTraitItem(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForTraitItem(filename: String, parentFullname: String, traitItemInstance: TraitItem): Ast = {
    if (traitItemInstance.constTraitItem.isDefined) {
      return astForTraitItemConst(filename, parentFullname, traitItemInstance.constTraitItem.get)
    } else if (traitItemInstance.fnTraitItem.isDefined) {
      return astForTraitItemFn(filename, parentFullname, traitItemInstance.fnTraitItem.get)
    } else if (traitItemInstance.typeTraitItem.isDefined) {
      return astForTraitItemType(filename, parentFullname, traitItemInstance.typeTraitItem.get)
    } else if (traitItemInstance.macroTraitItem.isDefined) {
      return astForTraitItemMacro(filename, parentFullname, traitItemInstance.macroTraitItem.get)
    } else if (traitItemInstance.verbatimTraitItem.isDefined) {
      return astForTokenStream(filename, parentFullname, traitItemInstance.verbatimTraitItem.get)
    } else {
      throw new RuntimeException("Unknown trait item type")
    }
  }

  def astForTraitItemConst(filename: String, parentFullname: String, traitItemConst: TraitItemConst): Ast = {
    val typeFullName = traitItemConst.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    val defaultCode = traitItemConst.default match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val localCode = s"const ${traitItemConst.ident}: ${typeFullName}"
    val fullCode  = s"${localCode} = ${defaultCode}"
    val constNode = localNode(traitItemConst, traitItemConst.ident, localCode, typeFullName)
    scope.addToScope(traitItemConst.ident, (constNode, typeFullName))

    val annotationsAst = traitItemConst.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = traitItemConst.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val idenAst = astForIdent(filename, parentFullname, traitItemConst.ident)
    val typeAst = traitItemConst.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val defaultAst = traitItemConst.default match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val assignmentNode = newOperatorCallNode(Operators.assignment, fullCode)

    callAst(assignmentNode, Seq(idenAst, typeAst, defaultAst))
      .withChild(Ast(constNode))
      // .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(annotationsAst)
  }

  def astForTraitItemFn(filename: String, parentFullname: String, traitItemFn: TraitItemFn): Ast = {
    val newMethodNode = methodNode(traitItemFn, traitItemFn.ident, traitItemFn.ident, "", filename)
    scope.addToScope(traitItemFn.ident, (newMethodNode, traitItemFn.ident))

    scope.pushNewScope(newMethodNode)

    val annotationsAst = traitItemFn.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = traitItemFn.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val parameterIns = traitItemFn.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
    val variadicAst = traitItemFn.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
    val methodRetNode = traitItemFn.output match {
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
    val defaultBodyAst = traitItemFn.default match {
      case Some(default) => astForBlock(filename, parentFullname, default)
      case None          => Ast()
    }

    scope.popScope()

    val methodAst = Ast(newMethodNode)
      .withChildren(parameterIns :+ variadicAst)
      .withChild(defaultBodyAst)
      .withChild(methodRetNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)

    methodAst

    // val node = memberNode(traitItemFn, traitItemFn.ident, "", "")
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(methodAst)
  }

  def astForTraitItemType(filename: String, parentFullname: String, traitItemType: TraitItemType): Ast = {
    var lhsCode = traitItemType.bounds.nonEmpty match {
      case true =>
        val boundsCode =
          traitItemType.bounds.map(bound => codeForTypeParamBound(filename, parentFullname, bound)).mkString(" + ")
        s"type ${traitItemType.ident}: ${boundsCode}"
      case false => s"type ${traitItemType.ident}"
    }
    val (code, newTypeDecl) = traitItemType.default match {
      case Some(default) =>
        val rhsCode  = typeFullnameForType(filename, parentFullname, default)
        val fullCode = s"$lhsCode = $rhsCode"
        val newTypeDecl = typeDeclNode(traitItemType, traitItemType.ident, traitItemType.ident, filename, fullCode)
          .aliasTypeFullName(rhsCode)
        (fullCode, newTypeDecl)
      case None => {
        val newTypeDecl = typeDeclNode(traitItemType, traitItemType.ident, traitItemType.ident, filename, lhsCode)
        (lhsCode, newTypeDecl)
      }
    }
    scope.addToScope(traitItemType.ident, (newTypeDecl, lhsCode))

    val annotationsAst = traitItemType.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = traitItemType.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val boundsAst = traitItemType.bounds.nonEmpty match {
      case true =>
        val boundsCode =
          traitItemType.bounds.map(codeForTypeParamBound(filename, parentFullname, _)).mkString(" + ")
        val wrapper = Ast(unknownNode(BoundAst(), boundsCode))
        wrapper.withChildren(traitItemType.bounds.map(astForTypeParamBound(filename, parentFullname, _)).toList)
      case false => Ast()
    }
    val defaultAst = traitItemType.default match {
      case Some(default) => astForType(filename, parentFullname, default)
      case None          => Ast()
    }

    Ast(newTypeDecl)
      .withChild(defaultAst)
      .withChild(genericsAst)
      .withChild(boundsAst)
      .withChildren(annotationsAst)

    // val node = memberNode(traitItemType, traitItemType.ident, lhsCode, traitItemType.ident)
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(Ast(newTypeDecl))
    //   .withChild(defaultAst)
    //   .withChild(genericsAst)
    //   .withChild(boundsAst)
    //   .withChildren(annotationsAst)
  }

  def astForTraitItemMacro(filename: String, parentFullname: String, traitItemMacro: TraitItemMacro): Ast = {
    val annotationsAst = traitItemMacro.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val macroInstance =
      Macro(traitItemMacro.path, traitItemMacro.delimiter, traitItemMacro.tokens)
    val macroAst = astForMacro(filename, parentFullname, macroInstance, traitItemMacro.semi_token)
      .withChildren(annotationsAst)

    macroAst

    // val node = memberNode(traitItemMacro, "", "", "")
    //   .astParentFullName("Member")
    //   .astParentType("Member")
    // Ast(node)
    //   .withChild(macroAst)
  }
}
