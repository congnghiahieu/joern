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
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.joern.x2cpg.utils.NodeBuilders.newOperatorCallNode
import io.shiftleft.codepropertygraph.generated.Operators

trait AstForItem(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForItem(filename: String, parentFullname: String, itemInstance: Item): Ast = {
    if (itemInstance.constItem.isDefined) {
      return astForItemConst(filename, parentFullname, itemInstance.constItem.get)
    } else if (itemInstance.enumItem.isDefined) {
      return astForItemEnum(filename, parentFullname, itemInstance.enumItem.get)
    } else if (itemInstance.externCrateItem.isDefined) {
      return astForItemExternCrate(filename, parentFullname, itemInstance.externCrateItem.get)
    } else if (itemInstance.fnItem.isDefined) {
      return astForItemFn(filename, parentFullname, itemInstance.fnItem.get)
    } else if (itemInstance.foreignModItem.isDefined) {
      return astForItemForeignMod(filename, parentFullname, itemInstance.foreignModItem.get)
    } else if (itemInstance.implItem.isDefined) {
      return astForItemImpl(filename, parentFullname, itemInstance.implItem.get)
    } else if (itemInstance.macroItem.isDefined) {
      return astForItemMacro(filename, parentFullname, itemInstance.macroItem.get)
    } else if (itemInstance.modItem.isDefined) {
      return astForItemMod(filename, parentFullname, itemInstance.modItem.get)
    } else if (itemInstance.staticItem.isDefined) {
      return astForItemStatic(filename, parentFullname, itemInstance.staticItem.get)
    } else if (itemInstance.structItem.isDefined) {
      return astForItemStruct(filename, parentFullname, itemInstance.structItem.get)
    } else if (itemInstance.traitItem.isDefined) {
      return astForItemTrait(filename, parentFullname, itemInstance.traitItem.get)
    } else if (itemInstance.traitAliasItem.isDefined) {
      return astForItemTraitAlias(filename, parentFullname, itemInstance.traitAliasItem.get)
    } else if (itemInstance.typeItem.isDefined) {
      return astForItemType(filename, parentFullname, itemInstance.typeItem.get)
    } else if (itemInstance.unionItem.isDefined) {
      return astForItemUnion(filename, parentFullname, itemInstance.unionItem.get)
    } else if (itemInstance.useItem.isDefined) {
      return astForItemUse(filename, parentFullname, itemInstance.useItem.get)
    } else if (itemInstance.verbatimItem.isDefined) {
      return astForTokenStream(filename, parentFullname, itemInstance.verbatimItem.get)
    } else {
      throw new RuntimeException("Unknown item type")
    }
  }

  def astForItemConst(filename: String, parentFullname: String, itemConst: ItemConst): Ast = {
    val typeFullName = itemConst.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    val exprCode = itemConst.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val localCode = s"const ${itemConst.ident}: ${typeFullName}"
    val fullCode  = s"${localCode} = ${exprCode}"
    val constNode = localNode(itemConst, itemConst.ident, localCode, typeFullName)
    scope.addToScope(itemConst.ident, (constNode, typeFullName))

    val annotationsAst = itemConst.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemConst.vis)
    val genericsAst = itemConst.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val identAst = astForIdent(filename, parentFullname, itemConst.ident)
    val typeAst = itemConst.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val exprAst = itemConst.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val assignmentNode = newOperatorCallNode(Operators.assignment, fullCode)

    callAst(assignmentNode, Seq(identAst, typeAst, exprAst))
      .withChild(Ast(constNode))
      // .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(annotationsAst)
  }

  def astForItemEnum(filename: String, parentFullname: String, itemEnum: ItemEnum): Ast = {
    val code        = s"enum ${itemEnum.ident}"
    val newEnumNode = typeDeclNode(itemEnum, itemEnum.ident, itemEnum.ident, filename, code)
    scope.addToScope(itemEnum.ident, (newEnumNode, code))

    scope.pushNewScope(newEnumNode)

    val annotationsAst = itemEnum.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemEnum.vis)
    val genericsAst = itemEnum.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val variants = itemEnum.variants.map(astForVariant(filename, itemEnum.ident, _)).toList

    scope.popScope()

    Ast(newEnumNode)
      // .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(variants)
      .withChildren(annotationsAst)
  }

  def astForItemExternCrate(filename: String, parentFullname: String, itemExternCrate: ItemExternCrate): Ast = {
    val annotationsAst = itemExternCrate.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemExternCrate.vis)

    val importedEntity = itemExternCrate.ident
    val importedAs     = itemExternCrate.rename.getOrElse(itemExternCrate.ident)
    var code = itemExternCrate.rename match {
      case Some(rename) => {
        s"extern crate ${importedEntity} as $importedAs;"
      }
      case None => s"extern crate ${importedEntity};"
    }
    code = modifierNode.modifierType match {
      case ModifierTypes.PUBLIC => s"pub $code"
      case _                    => code
    }
    val importNode = newImportNode(code, importedEntity, importedAs, itemExternCrate)

    Ast(
      unknownNode(WrapperAst(), code)
        .parserTypeName(classOf[ItemExternCrate].getSimpleName)
    )
      .withChild(Ast(importNode))
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemFn(filename: String, parentFullname: String, itemFn: ItemFn): Ast = {
    val newMethodNode = methodNode(itemFn, itemFn.ident, itemFn.ident, "", filename)
    scope.addToScope(itemFn.ident, (newMethodNode, itemFn.ident))

    scope.pushNewScope(newMethodNode)

    val annotationsAst = itemFn.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemFn.vis)
    val genericsAst = itemFn.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val parameterIns = itemFn.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
    val variadicAst = itemFn.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
    val methodRetNode = itemFn.output match {
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
    val bodyAst = astForBlock(filename, parentFullname, itemFn.stmts)

    scope.popScope()

    Ast(newMethodNode)
      .withChildren(parameterIns :+ variadicAst)
      .withChild(bodyAst)
      // .withChild(Ast(modifierNode))
      .withChild(methodRetNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
  }

  def astForItemForeignMod(filename: String, parentFullname: String, itemForeignMod: ItemForeignMod): Ast = {
    val abiName = nameForAbi(filename, parentFullname, itemForeignMod.abi)
    val code = itemForeignMod.unsafe match {
      case Some(true) => s"unsafe extern \"${abiName}\" {}"
      case _          => s"extern \"${abiName}\" {}"
    }

    // Items in extern blocks (for example: extern "C") still in the same scope as other items
    // Hence no `scope.pushNewScope(...)`

    val annotationsAst = itemForeignMod.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val foreignItemAst = itemForeignMod.items.map(astForForeignItem(filename, parentFullname, _)).toList

    Ast(
      unknownNode(WrapperAst(), code)
        .parserTypeName(classOf[ItemForeignMod].getSimpleName)
    )
      .withChildren(annotationsAst)
      .withChildren(foreignItemAst)
  }

  def astForItemImpl(filename: String, parentFullname: String, itemImpl: ItemImpl): Ast = {
    val structName = itemImpl.self_ty match {
      case Some(self_ty) => typeFullnameForType(filename, parentFullname, self_ty)
      case None          => Defines.Unknown
    }
    var code = itemImpl.traitImpl match {
      case Some((_, path)) => {
        val traitName = typeFullnameForPath(filename, parentFullname, path)
        s"impl ${traitName} for ${structName}"
      }
      case None => s"impl ${structName}"
    }
    if (itemImpl.unsafe.getOrElse(false)) { code = s"unsafe ${code}" }
    val implNode = typeDeclNode(itemImpl, code, code, filename, code)
    scope.addToScope(code, (implNode, code))

    scope.pushNewScope(implNode)

    val annotationsAst = itemImpl.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemImpl.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val selfTypeAst = itemImpl.self_ty match {
      case Some(self_ty) => astForType(filename, parentFullname, self_ty)
      case None          => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val traitImplAst = itemImpl.traitImpl match {
      case Some((_, path)) => astForPath(filename, parentFullname, path)
      case None            => Ast()
    }
    val itemsAst = itemImpl.items.map(astForImplItem(filename, parentFullname, _)).toList

    scope.popScope()

    Ast(implNode)
      .withChild(traitImplAst)
      .withChild(selfTypeAst)
      .withChildren(itemsAst)
      .withChild(genericsAst)
      .withChildren(annotationsAst)
  }

  def astForItemMacro(filename: String, parentFullname: String, itemMacro: ItemMacro): Ast = {
    val annotationsAst = itemMacro.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val macroInstance = Macro(itemMacro.path, itemMacro.delimiter, itemMacro.tokens)
    astForMacro(filename, parentFullname, macroInstance, itemMacro.semi_token, itemMacro.ident)
      .withChildren(annotationsAst)
  }

  def astForItemMod(filename: String, parentFullname: String, itemMod: ItemMod): Ast = {
    val annotationsAst = itemMod.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemMod.vis)

    itemMod.semi match {
      case Some(true) => {
        var code = s"mod ${itemMod.ident};"
        if (itemMod.unsafe.getOrElse(false)) { code = s"unsafe ${code}" }
        if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

        val importNode = newImportNode(code, itemMod.ident, itemMod.ident, itemMod)

        Ast(
          unknownNode(WrapperAst(), code)
            .parserTypeName(classOf[ItemMod].getSimpleName)
        )
          .withChild(Ast(importNode))
          // .withChild(Ast(modifierNode))
          .withChildren(annotationsAst)
      }
      case _ =>
        var code = s"mod ${itemMod.ident} {}"
        if (itemMod.unsafe.getOrElse(false)) { code = s"unsafe ${code}" }
        if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

        val modNamespaceBlock = NewNamespaceBlock()
          .name(itemMod.ident)
          .fullName(itemMod.ident)
          .filename(filename)
          .code(code)
        val modNamespaceAst = Ast(modNamespaceBlock)

        namespaceStack.push(modNamespaceBlock)
        scope.pushNewScope(modNamespaceBlock)

        val contentAst = itemMod.content match {
          case Some(content) => content.map(astForItem(filename, parentFullname, _)).toList
          case None          => List()
        }

        scope.popScope()
        namespaceStack.pop()

        modNamespaceAst
          // .withChild(Ast(modifierNode))
          .withChildren(contentAst)
          .withChildren(annotationsAst)
    }
  }

  def astForItemStatic(filename: String, parentFullname: String, itemStatic: ItemStatic): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemStatic.vis)

    val typeFullname = itemStatic.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    var code = itemStatic.mut match {
      case Some(StaticMutability.Mut) => s"static mut ${itemStatic.ident}: ${typeFullname}"
      case _                          => s"static ${itemStatic.ident}: ${typeFullname}"
    }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    code = itemStatic.expr match {
      case Some(expr) => s"${code} = ${codeForExpr(filename, parentFullname, expr)}"
      case None       => code
    }
    val staticNode = localNode(itemStatic, itemStatic.ident, code, typeFullname)
    scope.addToScope(itemStatic.ident, (staticNode, typeFullname))

    val annotationsAst = itemStatic.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val identAst = astForIdent(filename, parentFullname, itemStatic.ident)
    val typeAst = itemStatic.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val exprAst = itemStatic.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val assignmentNode = newOperatorCallNode(Operators.assignment, code)

    callAst(assignmentNode, Seq(identAst, typeAst, exprAst))
      .withChild(Ast(staticNode))
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemStruct(filename: String, parentFullname: String, itemStruct: ItemStruct): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemStruct.vis)
    var code         = s"struct ${itemStruct.ident}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

    val newItemStructNode = typeDeclNode(itemStruct, itemStruct.ident, itemStruct.ident, filename, code)
    scope.addToScope(itemStruct.ident, (newItemStructNode, code))

    scope.pushNewScope(newItemStructNode)

    val annotationsAst = itemStruct.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemStruct.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val fieldsAst = itemStruct.fields match {
      case Some(fields) => astForFields(filename, parentFullname, fields)
      case None         => List()
    }

    scope.popScope()

    Ast(newItemStructNode)
      .withChildren(annotationsAst)
      // .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(fieldsAst)
  }

  def astForItemTrait(filename: String, parentFullname: String, itemTrait: ItemTrait): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemTrait.vis)

    val superTraitCode = itemTrait.supertraits.map(codeForTypeParamBound(filename, parentFullname, _)).mkString(" + ")
    var code           = s"trait ${itemTrait.ident}"
    if (itemTrait.supertraits.nonEmpty) { code = s"${code}: ${superTraitCode}" }
    if (itemTrait.unsafe.getOrElse(false)) { code = s"unsafe $code" }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val newItemTraitNode = typeDeclNode(itemTrait, itemTrait.ident, itemTrait.ident, filename, code)
    scope.addToScope(itemTrait.ident, (newItemTraitNode, code))

    scope.pushNewScope(newItemTraitNode)

    val annotationsAst = itemTrait.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemTrait.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val supertraitsAst = itemTrait.supertraits.map(astForTypeParamBound(filename, parentFullname, _)).toList
    val traitItemsAst  = itemTrait.items.map(astForTraitItem(filename, parentFullname, _)).toList

    scope.popScope()

    Ast(newItemTraitNode)
      .withChildren(traitItemsAst)
      .withChild(genericsAst)
      .withChildren(supertraitsAst)
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemTraitAlias(filename: String, parentFullname: String, itemTraitAlias: ItemTraitAlias): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemTraitAlias.vis)

    var code = s"trait ${itemTraitAlias.ident}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val newItemTraitAliasNode = typeDeclNode(itemTraitAlias, itemTraitAlias.ident, itemTraitAlias.ident, filename, code)
    scope.addToScope(itemTraitAlias.ident, (newItemTraitAliasNode, code))

    scope.pushNewScope(newItemTraitAliasNode)

    val annotationsAst = itemTraitAlias.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemTraitAlias.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val boundsAst = itemTraitAlias.bounds.map(astForTypeParamBound(filename, parentFullname, _)).toList

    scope.popScope()

    Ast(newItemTraitAliasNode)
      .withChildren(annotationsAst)
      // .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(boundsAst)
  }

  def astForItemType(filename: String, parentFullname: String, itemType: ItemType): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemType.vis)

    var (node, code) = itemType.ty match {
      case Some(ty) => {
        val typeFullname = typeFullnameForType(filename, parentFullname, ty)
        val code         = s"type ${itemType.ident} = ${typeFullname}"
        val node =
          typeDeclNode(itemType, itemType.ident, itemType.ident, filename, code)
            .aliasTypeFullName(typeFullname)
        (node, code)
      }
      case None => {
        val code = s"type ${itemType.ident}"
        val node =
          typeDeclNode(itemType, itemType.ident, itemType.ident, filename, code)
        (node, code)
      }
    }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    scope.addToScope(itemType.ident, (node, code))

    scope.pushNewScope(node)

    val annotationsAst = itemType.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemType.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val typeAst = itemType.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    scope.popScope()

    Ast(node)
      .withChild(typeAst)
      .withChild(genericsAst)
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemUnion(filename: String, parentFullname: String, itemUnion: ItemUnion): Ast = {
    val modifierNode = modifierForVisibility(filename, parentFullname, itemUnion.vis)

    var code = s"union ${itemUnion.ident} {}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val newItemUnionNode = typeDeclNode(itemUnion, itemUnion.ident, itemUnion.ident, filename, code)
    scope.addToScope(itemUnion.ident, (newItemUnionNode, code))

    scope.pushNewScope(newItemUnionNode)

    val annotationsAst = itemUnion.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val genericsAst = itemUnion.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val fieldsAst = itemUnion.fields match {
      case Some(fields) => fields.map(astForField(filename, classOf[FieldsNamed].getSimpleName, _)).toList
      case None         => List()
    }

    scope.popScope()

    Ast(newItemUnionNode)
      // .withChild(Ast(modifierNode))
      .withChildren(fieldsAst)
      .withChild(genericsAst)
      .withChildren(annotationsAst)
  }

  def astForItemUse(filename: String, parentFullname: String, itemUse: ItemUse): Ast = {
    val annotationsAst = itemUse.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemUse.vis)

    val treeCode = itemUse.tree match {
      case Some(tree) => codeForUseTree(filename, parentFullname, tree)
      case None       => Defines.Unknown
    }
    var code = itemUse.leading_colon match {
      case Some(true) => {
        s"use ::${treeCode};"
      }
      case _ => {
        s"use ${treeCode};"
      }
    }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val importedEntity = treeCode
    val importedAs     = treeCode

    val importNode = newImportNode(code, importedEntity, importedAs, itemUse)

    Ast(
      unknownNode(WrapperAst(), code)
        .parserTypeName(classOf[ItemUse].getSimpleName)
    )
      .withChild(Ast(importNode))
      // .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }
}
