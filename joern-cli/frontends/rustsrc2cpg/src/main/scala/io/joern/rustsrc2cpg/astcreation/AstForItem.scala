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
    val annotationsAst = itemConst.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemConst.vis)
    val genericsAst = itemConst.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    val typeAst = itemConst.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }
    val exprAst = itemConst.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val typeFullName = itemConst.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    val localCode    = s"const ${itemConst.ident}: ${typeFullName}"
    val newLocalNode = localNode(itemConst, itemConst.ident, localCode, typeFullName)
    localNodeMap.put(itemConst.ident, newLocalNode)

    val exprCode = itemConst.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val fullCode = s"${localCode} = ${exprCode}"

    Ast(unknownNode(itemConst, fullCode))
      .withChild(Ast(newLocalNode))
      .withChild(typeAst)
      .withChild(exprAst)
      .withChild(Ast(modifierNode))
      .withChild(genericsAst)
      .withChildren(annotationsAst)
  }

  def astForItemEnum(filename: String, parentFullname: String, itemEnum: ItemEnum): Ast = {
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

    val code        = s"enum ${itemEnum.ident}"
    val newEnumNode = typeDeclNode(itemEnum, itemEnum.ident, itemEnum.ident, filename, code)
    typeDeclMap.addOne(itemEnum.ident, newEnumNode)

    Ast(newEnumNode)
      .withChild(Ast(modifierNode))
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
    val code = itemExternCrate.rename match {
      case Some(rename) => s"${modifierNode.modifierType} extern crate ${importedEntity} as ${importedAs};"
      case None         => s"${modifierNode.modifierType} extern crate ${importedEntity};"
    }

    val importNode = newImportNode(code, importedEntity, importedAs, itemExternCrate)

    Ast(importNode)
    // .withChild(Ast(modifierNode))
    // .withChildren(annotationsAst)
  }

  def astForItemFn(filename: String, parentFullname: String, itemFn: ItemFn): Ast = {
    val newMethodNode = methodNode(itemFn, itemFn.ident, itemFn.ident, "", filename).isExternal(itemFn.stmts.isEmpty)
    methodNodeMap.put(itemFn.ident, newMethodNode)

    val annotationsAst = itemFn.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val bodyAst = astForBlock(filename, parentFullname, itemFn.stmts)
    val parameterIns = itemFn.inputs.zipWithIndex.map { case (input, index) =>
      astForFnArg(filename, parentFullname, input, index)
    }.toList
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
    val variadicAst = itemFn.variadic match {
      case Some(variadic) => astForVariadic(filename, parentFullname, variadic)
      case _              => Ast()
    }
    val genericsAst = itemFn.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    val modifierNode = modifierForVisibility(filename, parentFullname, itemFn.vis)

    Ast(newMethodNode)
      .withChildren(parameterIns :+ variadicAst)
      .withChild(bodyAst)
      .withChild(Ast(modifierNode))
      .withChild(methodRetNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
  }

  def astForItemForeignMod(filename: String, parentFullname: String, itemForeignMod: ItemForeignMod): Ast = {
    val annotationsAst = itemForeignMod.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val abiName  = nameForAbi(filename, parentFullname, itemForeignMod.abi)
    val isUnsafe = itemForeignMod.unsafe.getOrElse(false)
    val code = if (isUnsafe) { s"unsafe extern \"${abiName}\" {}" }
    else { s"extern {}" }

    val foreignNamespaceBlock = NewNamespaceBlock()
      .name(abiName)
      .fullName(abiName)
      .filename(filename)
      .code(code)
    val foreignNamespaceAst = Ast(foreignNamespaceBlock)

    namespaceStack.push(foreignNamespaceBlock)
    scope.pushNewScope(foreignNamespaceBlock)
    val foreignItemAst = itemForeignMod.items.map(astForForeignItem(filename, parentFullname, _)).toList
    scope.popScope()
    namespaceStack.pop()

    foreignNamespaceAst
      .withChildren(annotationsAst)
      .withChildren(foreignItemAst)
  }

  def astForItemImpl(filename: String, parentFullname: String, itemImpl: ItemImpl): Ast = {
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

    val structName = itemImpl.self_ty match {
      case Some(self_ty) => typeFullnameForType(filename, parentFullname, self_ty)
      case None          => Defines.Unknown
    }
    val code = itemImpl.traitImpl match {
      case Some((_, path)) => {
        val traitName = typeFullnameForPath(filename, parentFullname, path)
        s"impl ${traitName} for ${structName}"
      }
      case None => s"impl ${structName}"
    }
    val implNode = typeDeclNode(itemImpl, code, code, filename, code)

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
    astForMacro(filename, parentFullname, macroInstance).withChildren(annotationsAst)
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

        Ast(importNode)
      }
      // .withChild(Ast(modifierNode))
      // .withChildren(annotationsAst)
      case _ =>
        var code = s"mod ${itemMod.ident} {}"
        if (itemMod.unsafe.getOrElse(false)) { code = s"unsafe ${code}" }
        if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

        val contentAst = itemMod.content match {
          case Some(content) =>
            val contentBlock    = blockAst(blockNode(WrapperAst()))
            val contentItemAsts = content.map(astForItem(filename, parentFullname, _)).toList
            contentBlock.withChildren(contentItemAsts)
          case None => Ast()
        }

        val modNamespaceBlock = NewNamespaceBlock()
          .name(itemMod.ident)
          .fullName(itemMod.ident)
          .filename(filename)
          .code(code)
        val modNamespaceAst = Ast(modNamespaceBlock)

        namespaceStack.push(modNamespaceAst.root.get)
        scope.pushNewScope(modNamespaceBlock)

        scope.popScope()
        namespaceStack.pop()

        modNamespaceAst
          .withChild(Ast(modifierNode))
          .withChild(contentAst)
          .withChildren(annotationsAst)
    }
  }

  def astForItemStatic(filename: String, parentFullname: String, itemStatic: ItemStatic): Ast = {
    val annotationsAst = itemStatic.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemStatic.vis)

    val typeFullname = itemStatic.ty.map(typeFullnameForType(filename, parentFullname, _)).getOrElse("")

    val isMut = itemStatic.mut.contains(StaticMutability.Mut)
    var code = if (isMut) { s"static mut ${itemStatic.ident}: ${typeFullname}" }
    else { s"static ${itemStatic.ident}: ${typeFullname}" }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

    val newLocalNode = localNode(itemStatic, itemStatic.ident, code, typeFullname)
    localNodeMap.put(itemStatic.ident, newLocalNode)

    Ast(unknownNode(itemStatic, ""))
      .withChild(Ast(newLocalNode))
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemStruct(filename: String, parentFullname: String, itemStruct: ItemStruct): Ast = {
    val annotationsAst = itemStruct.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemStruct.vis)
    val genericsAst = itemStruct.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    var code = s"struct ${itemStruct.ident}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val fieldsAst = itemStruct.fields match {
      case Some(fields) => astForFields(filename, parentFullname, fields)
      case None         => Ast()
    }

    val newItemStructNode = typeDeclNode(itemStruct, itemStruct.ident, itemStruct.ident, filename, code)
    typeDeclMap.addOne(itemStruct.ident, newItemStructNode)

    Ast(newItemStructNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
      .withChild(fieldsAst)
  }

  def astForItemTrait(filename: String, parentFullname: String, itemTrait: ItemTrait): Ast = {
    val annotationsAst = itemTrait.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemTrait.vis)
    val genericsAst = itemTrait.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val supertraitsAst =
      itemTrait.supertraits.map(astForTypeParamBound(filename, parentFullname, _)).toList
    val traitItemsAst = itemTrait.items.map(astForTraitItem(filename, parentFullname, _)).toList

    val superTraitCode = itemTrait.supertraits.map(codeForTypeParamBound(filename, parentFullname, _)).mkString(" + ")

    var code = s"trait ${itemTrait.ident}"
    if (itemTrait.supertraits.nonEmpty) {
      code = s"${code}: ${superTraitCode}"
    }
    if (itemTrait.unsafe.getOrElse(false)) {
      code = s"unsafe $code"
    }
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

    val newItemTraitNode = typeDeclNode(itemTrait, itemTrait.ident, itemTrait.ident, filename, code)
    typeDeclMap.addOne(itemTrait.ident, newItemTraitNode)

    Ast(newItemTraitNode)
      .withChildren(traitItemsAst)
      .withChild(genericsAst)
      .withChildren(supertraitsAst)
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemTraitAlias(filename: String, parentFullname: String, itemTraitAlias: ItemTraitAlias): Ast = {
    val annotationsAst = itemTraitAlias.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemTraitAlias.vis)
    val genericsAst = itemTraitAlias.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    var code = s"trait ${itemTraitAlias.ident}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }

    val boundsAst =
      itemTraitAlias.bounds.map(astForTypeParamBound(filename, parentFullname, _)).toList

    val newItemTraitAliasNode = typeDeclNode(itemTraitAlias, itemTraitAlias.ident, itemTraitAlias.ident, filename, code)
    typeDeclMap.addOne(itemTraitAlias.ident, newItemTraitAliasNode)

    Ast(newItemTraitAliasNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
      .withChildren(boundsAst)
  }

  def astForItemType(filename: String, parentFullname: String, itemType: ItemType): Ast = {
    val annotationsAst = itemType.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemType.vis)
    val genericsAst = itemType.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
    val typeAst = itemType.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    val typeFullname = itemType.ty match {
      case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
      case None     => Defines.Unknown
    }
    var code = s"type ${itemType.ident} = ${typeFullname}"
    if (modifierNode.modifierType == ModifierTypes.PUBLIC) { code = s"pub ${code}" }
    val newItemTypeNode =
      typeDeclNode(itemType, itemType.ident, itemType.ident, filename, code)
        .aliasTypeFullName(typeFullname)
    typeDeclMap.addOne(itemType.ident, newItemTypeNode)

    Ast(newItemTypeNode)
      .withChild(typeAst)
      .withChild(genericsAst)
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }

  def astForItemUnion(filename: String, parentFullname: String, itemUnion: ItemUnion): Ast = {
    val annotationsAst = itemUnion.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val modifierNode = modifierForVisibility(filename, parentFullname, itemUnion.vis)
    val genericsAst = itemUnion.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }

    val code            = s"union ${itemUnion.ident} {}"
    val newItemTypeNode = typeDeclNode(itemUnion, itemUnion.ident, itemUnion.ident, filename, code)
    typeDeclMap.addOne(itemUnion.ident, newItemTypeNode)

    Ast(newItemTypeNode)
      .withChildren(annotationsAst)
      .withChild(genericsAst)
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

    Ast(unknownNode(itemUse, ""))
      .withChild(Ast(importNode))
      .withChild(Ast(modifierNode))
      .withChildren(annotationsAst)
  }
}
