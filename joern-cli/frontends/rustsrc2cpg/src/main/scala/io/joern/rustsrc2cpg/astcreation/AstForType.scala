package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.rustsrc2cpg.ast.Type
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

trait AstForType(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForType(filename: String, parentFullname: String, typeInstance: Type): Ast = {
    typeInstance match {
      case typeNoValue: TypeNoValue =>
        val node = typeRefNode(typeNoValue, typeNoValue.toString, typeNoValue.toString)
        Ast(node)
      case typeHasValue: TypeHasValue =>
        astForTypeHasValue(filename, parentFullname, typeHasValue)
    }
  }

  def astForTypeHasValue(filename: String, parentFullname: String, typeHasValueInstance: TypeHasValue): Ast = {
    if (typeHasValueInstance.arrayType.isDefined) {
      astForTypeArray(filename, parentFullname, typeHasValueInstance.arrayType.get)
    } else if (typeHasValueInstance.bareFnType.isDefined) {
      astForTypeBareFn(filename, parentFullname, typeHasValueInstance.bareFnType.get)
    } else if (typeHasValueInstance.groupType.isDefined) {
      astForTypeGroup(filename, parentFullname, typeHasValueInstance.groupType.get)
    } else if (typeHasValueInstance.implTraitType.isDefined) {
      astForTypeImplTrait(filename, parentFullname, typeHasValueInstance.implTraitType.get)
    } else if (typeHasValueInstance.macroType.isDefined) {
      astForTypeMacro(filename, parentFullname, typeHasValueInstance.macroType.get)
    } else if (typeHasValueInstance.parenType.isDefined) {
      astForTypeParen(filename, parentFullname, typeHasValueInstance.parenType.get)
    } else if (typeHasValueInstance.pathType.isDefined) {
      astForTypePath(filename, parentFullname, typeHasValueInstance.pathType.get)
    } else if (typeHasValueInstance.ptrType.isDefined) {
      astForTypePtr(filename, parentFullname, typeHasValueInstance.ptrType.get)
    } else if (typeHasValueInstance.referenceType.isDefined) {
      astForTypeReference(filename, parentFullname, typeHasValueInstance.referenceType.get)
    } else if (typeHasValueInstance.sliceType.isDefined) {
      astForTypeSlice(filename, parentFullname, typeHasValueInstance.sliceType.get)
    } else if (typeHasValueInstance.traitObjectType.isDefined) {
      astForTypeTraitObject(filename, parentFullname, typeHasValueInstance.traitObjectType.get)
    } else if (typeHasValueInstance.tupleType.isDefined) {
      astForTypeTuple(filename, parentFullname, typeHasValueInstance.tupleType.get)
    } else if (typeHasValueInstance.verbatimType.isDefined) {
      astForTokenStream(filename, parentFullname, typeHasValueInstance.verbatimType.get)
    } else {
      throw new IllegalArgumentException("Unsupported type has value instance")
    }
  }

  def astForTypeArray(filename: String, parentFullname: String, typeArrayInstance: TypeArray): Ast = {
    val typeFullname = typeFullnameForTypeArray(filename, parentFullname, typeArrayInstance)
    val node         = typeRefNode(typeArrayInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeBareFn(filename: String, parentFullname: String, typeBareFnInstance: TypeBareFn): Ast = {
    val typeFullname = typeFullnameForTypeBareFn(filename, parentFullname, typeBareFnInstance)
    val node         = typeRefNode(typeBareFnInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeGroup(filename: String, parentFullname: String, typeGroupInstance: TypeGroup): Ast = {
    val typeFullname = typeFullnameForTypeGroup(filename, parentFullname, typeGroupInstance)
    val node         = typeRefNode(typeGroupInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeImplTrait(filename: String, parentFullname: String, typeImplTraitInstance: TypeImplTrait): Ast = {
    val typeFullname = typeFullnameForTypeImplTrait(filename, parentFullname, typeImplTraitInstance)
    val node         = typeRefNode(typeImplTraitInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeMacro(filename: String, parentFullname: String, typeMacroInstance: TypeMacro): Ast = {
    val typeFullname = typeFullnameForTypeMacro(filename, parentFullname, typeMacroInstance)
    val node         = typeRefNode(typeMacroInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeParen(filename: String, parentFullname: String, typeParenInstance: TypeParen): Ast = {
    val typeFullname = typeFullnameForTypeParen(filename, parentFullname, typeParenInstance)
    val node         = typeRefNode(typeParenInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypePath(filename: String, parentFullname: String, typePathInstance: TypePath): Ast = {
    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val path = Path(typePathInstance.segments, typePathInstance.leading_colon)
    astForPath(filename, parentFullname, path, typePathInstance.qself)
  }

  def astForTypePtr(filename: String, parentFullname: String, typePtrInstance: TypePtr): Ast = {
    val typeFullname = typeFullnameForTypePtr(filename, parentFullname, typePtrInstance)

    val node = typeRefNode(typePtrInstance, typeFullname, typeFullname)

    Ast(node)
  }

  def astForTypeReference(filename: String, parentFullname: String, typeReferenceInstance: TypeReference): Ast = {
    val typeFullname = typeFullnameForTypeReference(filename, parentFullname, typeReferenceInstance)
    val wrapper      = unknownNode(typeReferenceInstance, typeFullname)
    val lifetimeAst = typeReferenceInstance.lifetime match {
      case Some(lifetime) => {
        val (ast, node) = astForLifetime(filename, parentFullname, lifetime)
        diffGraph.addEdge(wrapper, node, EdgeTypes.OUT_LIVE)
        ast
      }
      case None => Ast()
    }
    val elem = typeReferenceInstance.elem match {
      case Some(elem) => astForType(filename, parentFullname, elem)
      case None       => Ast()
    }

    Ast(wrapper)
      .withChild(lifetimeAst)
      .withChild(elem)
  }

  def astForTypeSlice(filename: String, parentFullname: String, typeSliceInstance: TypeSlice): Ast = {
    val typeFullname = typeFullnameForTypeSlice(filename, parentFullname, typeSliceInstance)
    val node         = typeRefNode(typeSliceInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeTraitObject(filename: String, parentFullname: String, typeTraitObjectInstance: TypeTraitObject): Ast = {
    val typeFullname = typeFullnameForTypeTraitObject(filename, parentFullname, typeTraitObjectInstance)
    val node         = typeRefNode(typeTraitObjectInstance, typeFullname, typeFullname)
    Ast(node)
  }

  def astForTypeTuple(filename: String, parentFullname: String, typeTupleInstance: TypeTuple): Ast = {
    val typeFullname = typeFullnameForTypeTuple(filename, parentFullname, typeTupleInstance)
    val node         = typeRefNode(typeTupleInstance, typeFullname, typeFullname)
    Ast(node)
  }
}

trait TypeFullnameForType(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def typeFullnameForType(filename: String, parentFullname: String, typeInstance: Type): String = {
    val typeFullname = typeInstance match {
      case typeNoValue: TypeNoValue =>
        typeNoValue.toString
      case typeHasValue: TypeHasValue =>
        typeFullnameForTypeHasValue(filename, parentFullname, typeHasValue)
    }
    typeFullname
  }

  def typeFullnameForTypeHasValue(
    filename: String,
    parentFullname: String,
    typeHasValueInstance: TypeHasValue
  ): String = {
    val typeFullname = if (typeHasValueInstance.arrayType.isDefined) {
      typeFullnameForTypeArray(filename, parentFullname, typeHasValueInstance.arrayType.get)
    } else if (typeHasValueInstance.bareFnType.isDefined) {
      typeFullnameForTypeBareFn(filename, parentFullname, typeHasValueInstance.bareFnType.get)
    } else if (typeHasValueInstance.groupType.isDefined) {
      typeFullnameForTypeGroup(filename, parentFullname, typeHasValueInstance.groupType.get)
    } else if (typeHasValueInstance.implTraitType.isDefined) {
      typeFullnameForTypeImplTrait(filename, parentFullname, typeHasValueInstance.implTraitType.get)
    } else if (typeHasValueInstance.macroType.isDefined) {
      typeFullnameForTypeMacro(filename, parentFullname, typeHasValueInstance.macroType.get)
    } else if (typeHasValueInstance.parenType.isDefined) {
      typeFullnameForTypeParen(filename, parentFullname, typeHasValueInstance.parenType.get)
    } else if (typeHasValueInstance.pathType.isDefined) {
      typeFullnameForTypePath(filename, parentFullname, typeHasValueInstance.pathType.get)
    } else if (typeHasValueInstance.ptrType.isDefined) {
      typeFullnameForTypePtr(filename, parentFullname, typeHasValueInstance.ptrType.get)
    } else if (typeHasValueInstance.referenceType.isDefined) {
      typeFullnameForTypeReference(filename, parentFullname, typeHasValueInstance.referenceType.get)
    } else if (typeHasValueInstance.sliceType.isDefined) {
      typeFullnameForTypeSlice(filename, parentFullname, typeHasValueInstance.sliceType.get)
    } else if (typeHasValueInstance.traitObjectType.isDefined) {
      typeFullnameForTypeTraitObject(filename, parentFullname, typeHasValueInstance.traitObjectType.get)
    } else if (typeHasValueInstance.tupleType.isDefined) {
      typeFullnameForTypeTuple(filename, parentFullname, typeHasValueInstance.tupleType.get)
    } else if (typeHasValueInstance.verbatimType.isDefined) {
      codeForTokenStream(filename, parentFullname, typeHasValueInstance.verbatimType.get)
    } else {
      throw new IllegalArgumentException("Unsupported type has value instance")
    }
    typeFullname
  }

  def typeFullnameForTypeArray(filename: String, parentFullname: String, typeArrayInstance: TypeArray): String = {
    if (!typeArrayInstance.elem.isDefined) {
      return ""
    }

    val elemenTypeFullname = typeFullnameForType(filename, parentFullname, typeArrayInstance.elem.get)

    val typeFullname = typeArrayInstance.len match {
      case Some(len) => {
        val lenCode = codeForExpr(filename, parentFullname, len)
        s"[$elemenTypeFullname; $lenCode]"
      }
      case None => s"[$elemenTypeFullname]"
    }

    typeFullname
  }

  def typeFullnameForTypeBareFn(filename: String, parentFullname: String, typeBareFnInstance: TypeBareFn): String = {
    val inputsCode = typeBareFnInstance.inputs.map(codeForBareFnArg(filename, parentFullname, _)).toList
    val variadicCode = typeBareFnInstance.variadic match {
      case Some(variadic) => List(codeForBareVariadic(filename, parentFullname, variadic))
      case _              => List()
    }
    val totalInputCode = (inputsCode ++ variadicCode).mkString(", ")

    var code = typeBareFnInstance.output match {
      case Some(output) => {
        val outputCode = typeFullnameForType(filename, parentFullname, output)
        s"fn($totalInputCode) -> $outputCode"
      }
      case None => s"fn($totalInputCode)"
    }

    code = typeBareFnInstance.unsafe match {
      case Some(true) => s"unsafe $code"
      case _          => code
    }

    code = typeBareFnInstance.lifetimes match {
      case Some(boundLifetimes) => {
        val boundLifetimeCode =
          s"for <${boundLifetimes.map(codeForGenericParam(filename, parentFullname, _)).mkString(", ")}>"
        s"$boundLifetimeCode $code"
      }
      case None => code
    }

    code
  }

  def typeFullnameForTypeGroup(filename: String, parentFullname: String, typeGroupInstance: TypeGroup): String = {
    // NOTE: Just temporary, need to find out what "TypeGroup" (in rust code form) is
    val typeFullname = typeGroupInstance.elem match {
      case Some(elem) => typeFullnameForType(filename, parentFullname, elem)
      case None       => Defines.Unknown
    }
    s"($typeFullname)"
  }

  def typeFullnameForTypeImplTrait(
    filename: String,
    parentFullname: String,
    typeImplTraitInstance: TypeImplTrait
  ): String = {
    val typeFullname =
      typeImplTraitInstance.bounds.map(codeForTypeParamBound(filename, parentFullname, _)).mkString(" + ")
    s"impl $typeFullname"
  }

  def typeFullnameForTypeMacro(filename: String, parentFullname: String, typeMacroInstance: TypeMacro): String = {
    val macroInstance = Macro(typeMacroInstance.path, typeMacroInstance.delimiter, typeMacroInstance.tokens)
    val (_, _, code)  = codeForMacro(filename, parentFullname, macroInstance)
    code
  }

  def typeFullnameForTypeParen(filename: String, parentFullname: String, typeParenInstance: TypeParen): String = {
    if (!typeParenInstance.elem.isDefined) {
      return ""
    }

    val elemenTypeFullname = typeFullnameForType(filename, parentFullname, typeParenInstance.elem.get)

    val typeFullname = s"($elemenTypeFullname)"
    typeFullname
  }

  def typeFullnameForTypePath(filename: String, parentFullname: String, typePathInstance: TypePath): String = {
    val typeFullname =
      typeFullnameForPath(
        filename,
        parentFullname,
        Path(typePathInstance.segments, typePathInstance.leading_colon),
        typePathInstance.qself
      )
    typeFullname
  }

  def typeFullnameForTypePtr(filename: String, parentFullname: String, typePtrInstance: TypePtr): String = {
    if (!typePtrInstance.elem.isDefined) {
      return ""
    }

    val elemenTypeFullname = typeFullnameForType(filename, parentFullname, typePtrInstance.elem.get)

    val typeFullname = if (typePtrInstance.mut.getOrElse(false)) {
      s"*mut $elemenTypeFullname"
    } else if (typePtrInstance.const.getOrElse(false)) {
      s"*const $elemenTypeFullname"
    } else {
      ""
    }
    typeFullname
  }

  def typeFullnameForTypeReference(
    filename: String,
    parentFullname: String,
    typeReferenceInstance: TypeReference
  ): String = {
    var code = typeReferenceInstance.lifetime match {
      case Some(lifetime) => {
        val lifetimeCode = codeForLifetime(filename, parentFullname, lifetime)
        s"&$lifetimeCode"
      }
      case None => "&"
    }
    code = typeReferenceInstance.mut match {
      case Some(true) => s"${code} mut"
      case _          => code
    }
    code = typeReferenceInstance.elem match {
      case Some(elem) => {
        val typeFullname = typeFullnameForType(filename, parentFullname, elem)
        s"${code} $typeFullname"
      }
      case None => code
    }

    code
  }

  def typeFullnameForTypeSlice(filename: String, parentFullname: String, typeSliceInstance: TypeSlice): String = {
    if (!typeSliceInstance.elem.isDefined) {
      return ""
    }

    val elemenTypeFullname = typeFullnameForType(filename, parentFullname, typeSliceInstance.elem.get)

    val typeFullname = s"[$elemenTypeFullname]"
    typeFullname
  }

  def typeFullnameForTypeTraitObject(
    filename: String,
    parentFullname: String,
    typeTraitObjectInstance: TypeTraitObject
  ): String = {
    val boundsCode =
      typeTraitObjectInstance.bounds.map(codeForTypeParamBound(filename, parentFullname, _)).mkString(" + ")
    val typeFullname = typeTraitObjectInstance.dyn match {
      case Some(true) => s"dyn $boundsCode"
      case _          => boundsCode
    }
    typeFullname
  }

  def typeFullnameForTypeTuple(filename: String, parentFullname: String, typeTupleInstance: TypeTuple): String = {
    val tupleTypes   = typeTupleInstance.elems.map(typeFullnameForType(filename, parentFullname, _))
    val typeFullname = s"(${tupleTypes.mkString(", ")})"
    typeFullname
  }
}
