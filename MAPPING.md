# Syn AST

- https://docs.rs/syn/latest/syn/index.html#structs
- https://docs.rs/syn/latest/syn/index.html#enums

# Joern CPG

- https://cpg.joern.io

# Syn AST -> Joern CPG

# Auto gen

- `FILE`
- `SOURCE_FILE`
- `NAMESPACE`

# Manual gen

- ... -> `CODE`, `FULL_NAME`, `INDEX`, `IS_EXTERNAL`, `NAME`
- ... -> `META_DATA`, `LANGUAGE`, `ROOT`, `OVERLAYS`
- ... -> `FILENAME` field
- ... -> `METHOD_REF`, `TYPE_REF`

# Prompt

- generate a class generic, function generic and the usage example in java

- generate a main function contains 1 conventional if elseif else, 1 conventional for loop, 1 conventional while in js

convert below rust to scala in these line, with NOTE what:

- using `class`
- declare variable inside class curly brace
- do not generate constructor
- use None value for Option type
- Use `ListBuffer` to replace `Vec` and `Punctuated`
- When you see attribute `skip_serializing_if`, use Option as type wrapper
- When you see attribute `#[serde(flatten)]`, using `@JsonUnwrapped` property of jackson for that field
- When you see attribute `#[serde(transparent)]`, generate a comment above that variable
- Replace `Box` type with `Option` in scala
- Special notice for `rename` attribute in `#[serde(rename = "...")]`. For example, have `#[serde(rename = "stmts")]`, so use `stmts` as variable name
- Do not use `defaultness`, use `default`
- Do not use `unsafety`, use `unsafe`
- Do nut use `mutability` use `mut`
- Dot not use `block` use `stmts`

- We already have `attrs`, skip it

#file:Item.scala apply all class (from ItemForeignMod) variable to these case, note that:

Given class, apply variable to these case, note that

- ignore variable `attrs`
- use variable name instead of `case _:`
- see above examples
- ignore variable that have been commented
- ignore variable `mac`, `unsafe`, `ident`, `move`, `label`, `const`, `static`, `async`, `dot2_token`, `lifetime`
- ignore variable that have type `String`, `Indent`, `Index`, `Option[Boolean]`, `Boolean`
- For type `Item`, use `mapParentForItem`
- For type `Visibility`, use `mapParentForVisibility`
- For type `Fields`, use `mapParentForFields`
- For type `FieldsNamed`, use `mapParentForFieldsNamed`
- For type `FieldsUnnamed`, use `mapParentForFieldsUnnamed`
- For type `Type`, use `mapParentForType`
- For type `Pat`, use `mapParentForPat`
- For type `Expr`, use `mapParentForExpr`
- For type `Stmt`, use `mapParentForStmt`
- For type `FnArg`, use `mapParentForFnArg`
- For type `ReturnType`, use `mapParentForReturnType`
- For type `BoundLifetimes`, use `mapParentForBoundLifetimes`
- For type `GenericParam`, use `mapParentForGenericParam`
- For type `WherePredicate`, use `mapParentForWherePredicate`
- For type `TokenStream`, use `mapParentForTokenStream`
- For type `TokenTree`, use `mapParentForTokenTree`
- For type `ForeignItem`, use `mapParentForForeignItem`
- For type `ImplItem`, use `mapParentForImplItem`
- For type `TypeParamBound`, use `mapParentForTypeParamBound`
- For type `TraitItem`, use `mapParentForTraitItem`
- For type `UseTree`, use `mapParentForUseTree`
- For type `GenericArgument`, use `mapParentForGenericArgument`
- For type `PathArguments`, use `mapParentForPathArguments`

- convert this to fit all below function, use `else if` instead of `if`. At the end put a `else` cause that throw error
- Use `astFor` function below, corresspoding their variable name

- see #, # (function ``) and # , generate compatible file with the same pattern
- convert this to fit all below function, use `else if` instead of `if`. At the end put a `else` cause that throw error
- Do not use `addChild`

- generate 1 more trait CodeFor... based on this trait, function return String instead of Ast

- With class of function, for example `ExprClosure` generate missing code for each property of class. See above function as examples

- create a variable equal `NewTypeArgument()`, then wrapped it in `Ast()`
- use `NewTypeArgument()` only, remove .name

- give related information to `TraitItemConst...`.
- create a variable equal `New...()` (NewLocal for example), then wrapped it in `Ast(NewMember())`
- Use Ast(NewMember()).withChild(...)
- Remove comment

- rename all function name to pattern `astForPat...`

- convert all the `if ... else Nil` to using flatmap to check Option
- Correspoding variable then using `withChildren` instead of `withChild`
- See this examples: `val annotationsAst = macroStmtInstance.attrs.toList.flatMap(_.map(astForAttribute(filename, parentFullname, _)))`

For each function, generate the missing code:

- a variable typeFullname equal to corresspoding `typeFullnameForType...` of function
- a variable node equal `NewTypeRef()` with `.typeFullname` set to variable `typeFullname` above
- return `Ast(node)`
- see function `astForTypeArray` for example

```
- Convert all code using `.toList.flatMap(_.map)` to using `match`

For example from this:
`val annotationsAst = arrayExprInstance.attrs.toList.flatMap(_.map(astForAttribute(filename, parentFullname, _)))`

Convert to:
`val annotationsAst = arrayExprInstance.attrs match {
case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, \_)).toList
case None => List()
}`

add annotationsAst vairable for all of these

For example:

`val annotationsAst = arrayExprInstance.attrs match {
case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
case None => List()
}`

Combile with return Ast, for example

controlStructureAst(armNode, Some(conditionAst), bodyAst)
.withChildren(annotationsAst)

move `val annotationsAst` to head of function body
```

- convert all

```
val genericsAst = typeImplItemInstance.generics match {
      case Some(generics) => List(astForGenerics(filename, parentFullname, generics))
      case None           => List()
    }
```

to

```
val genericsAst = typeImplItemInstance.generics match {
      case Some(generics) => astForGenerics(filename, parentFullname, generics)
      case None           => Ast()
    }
```

and use `withChild` instead of `withChildren` for `genericsAst`

- replace `unknownNode(UnknownAst(), "")` with `unknownNode(variableName, "")`

For example: `unknownNode(UnknownAst(), "")` to `unknownNode(referencePatInstance, "")`

# Enum

- `AttrStyle` ->
- `BinOp` ->
- `Dataderive` ->
- `Expr` ->
- `FieldMutability` ->
- `Fields` ->
- `FnArg` ->
- `ForeignItem` ->
- `GenericArgument` ->
- `GenericParam` ->
- `ImplItem` ->
- `ImplRestriction` ->
- `Item` ->
- `Lit` ->
- `MacroDelimiter` ->
- `Member` ->
- `Meta` ->
- `Pat` ->
- `PathArguments` ->
- `RangeLimits` ->
- `ReturnType` ->
- `StaticMutability` ->
- `Stmt` ->
- `TraitBoundModifier` ->
- `TraitItem` ->
- `Type` ->
- `TypeParamBound` ->
- `UnOp` ->
- `UseTree` ->
- `Visibility` ->
- `WherePredicate` ->

# Struct

- `Abi` -> `NewNamespaceBlock` | `NewIdentifier`
- `AngleBracketedGenericArguments` -> `NewTypeArgument` | `NewTypeParameter`
- `Arm` -> `NewControlStructure`
- `AssocConst` -> `NewTypeArgument`
- `AssocType` -> `NewTypeArgument`
- `Attribute` -> `NewAnnotation`
- `BareFnArg` ->
- `BareVariadic` ->
- `Block` -> `NewBlock`
- `BoundLifetimes` -> `NewTypeParameter`
- `ConstParam` -> `NewTypeParameter`
- `Constraint` -> `NewTypeArgument`
- `ExprArray` -> `NewArrayInitializer`
- `ExprAssign` ->
- `ExprAsync` -> `NewBlock`
- `ExprAwait` -> `NewCall`
- `ExprBinary` ->
- `ExprBlock` -> `NewBlock`
- `ExprBreak` -> `NewControlStructure`
- `ExprCall` -> `NewCall`
- `ExprCast` -> `NewCall`
- `ExprClosure` -> `NewMethod`
- `ExprConst` -> `NewLocal`
- `ExprContinue` -> `NewControlStructure`
- `ExprField` -> `NewFieldIdentifier`
- `ExprForLoop` -> `NewControlStructure`
- `ExprGroup` ->
- `ExprIf` -> `NewControlStructure`
- `ExprIndex` -> `NewFieldIdentifier`
- `ExprInfer` -> `NewLocal`
- `ExprLet` -> `NewLocal`
- `ExprLit` -> `NewLiteral`
- `ExprLoop` -> `NewControlStructure`
- `ExprMacro` -> `NewCall`
- `ExprMatch` -> `NewControlStructure`
- `ExprMethodCall` -> `NewCall`
- `ExprParen` ->
- `ExprPath` -> `NewTypeRef` | `NewMethodRef`
- `ExprRange` -> `NewArrayInitializer`
- `ExprReference` -> `NewTypeRef`
- `ExprRepeat` -> `NewArrayInitializer`
- `ExprReturn` -> `NewReturn`
- `ExprStruct` -> `NewLocal`
- `ExprTry` -> `NewControlStructure`
- `ExprTryBlock` -> `NewBlock`
- `ExprTuple` -> `NewArrayInitializer`
- `ExprUnary` ->
- `ExprUnsafe` -> `NewBlock`
- `ExprWhile` -> `NewControlStructure`
- `ExprYield` -> `NewReturn`
- `Field` -> `NewMember`
- `FieldPat` -> `NewMember`
- `FieldValue` -> `NewMember`
- `FieldsNamed` ->
- `FieldsUnnamed` ->
- `File` -> `NewFile`
- `ForeignItemFn` -> `NewMember` && `NewMethod`
- `ForeignItemMacro` -> `NewMember` && `NewCall`
- `ForeignItemStatic` -> `NewMember` && `NewLocal`
- `ForeignItemType` -> `NewMember`&&`NewTypeDecl`
- `Generics` -> `NewTypeParameter`
- `Ident` -> `NewIdentifier`
- `ImplItemConst` -> `NewMember` && `NewLocal`
- `ImplItemFn` -> `NewMember` && `NewMethod`
- `ImplItemMacro` -> `NewMember` && `NewCall`
- `ImplItemType` -> `NewMember`&&`NewTypeDecl`
- `Index` -> `NewFieldIdentifier`
- `ItemConst` -> `NewLocal`
- `ItemEnum` -> `NewTypeDecl`
- `ItemExternCrate` -> `NewImport`
- `ItemFn` -> `NewMethod`
- `ItemForeignMod` -> `NewNamespaceBlock`
- `ItemImpl` -> `NewTypeDecl`
- `ItemMacro` -> `NewCall`
- `ItemMod` -> `NewImport` | `NewNamespaceBlock`
- `ItemStatic` -> `NewLocal`
- `ItemStruct` -> `NewTypeDecl`
- `ItemTrait` -> `NewTypeDecl`
- `ItemTraitAlias` -> `NewTypeDecl`
- `ItemType` -> `NewTypeDecl`
- `ItemUnion` -> `NewTypeDecl`
- `ItemUse` -> `NewImport`
- `Label` -> `NewJumpLabel`
- `Lifetime` -> `NewTypeArgument` | `NewTypeParameter`
- `LifetimeParam` -> `NewTypeParameter`
- `LitBool` -> `NewLiteral`
- `LitByte` -> `NewLiteral`
- `LitByteStr` -> `NewLiteral`
- `LitCStr` -> `NewLiteral`
- `LitChar` -> `NewLiteral`
- `LitFloat` -> `NewLiteral`
- `LitInt` -> `NewLiteral`
- `LitStr` -> `NewLiteral`
- `Local` -> `NewLocal`
- `LocalInit` ->
- `Macro` -> `NewCall`
- `MetaList` -> `NewAnnotationParameter`
- `MetaNameValue` -> `NewAnnotationParameterAssign`
- `ParenthesizedGenericArguments` -> `NewTypeArgument` | `NewTypeParameter`
- `PatConst` ->
- `PatIdent` ->
- `PatLit` ->
- `PatMacro` ->
- `PatOr` ->
- `PatParen` ->
- `PatPath` ->
- `PatRange` ->
- `PatReference` ->
- `PatRest` ->
- `PatSlice` ->
- `PatStruct` ->
- `PatTuple` ->
- `PatTupleStruct` ->
- `PatType` -> `NewMethodParameterIn`
- `PatWild` ->
- `Path` -> `NewTypeRef` | `NewMethodRef`
- `PathSegment` ->
- `PredicateLifetime` -> `NewTypeParameter`
- `PredicateType` -> `NewTypeParameter`
- `QSelf` -> `NewTypeParameter`
- `Receiver` -> `NewMethodParameterIn`
- `Signature` ->
- `StmtMacro` -> `NewCall`
- `TraitBound` -> `NewTypeParameter`
- `TraitItemConst` -> `NewMember` && `NewLocal`
- `TraitItemFn` -> `NewMember` && `NewMethod`
- `TraitItemMacro` -> `NewMember` && `NewCall`
- `TraitItemType` ->`NewMember` && `NewTypeDecl`
- `TypeArray` -> `NewTypeRef`
- `TypeBareFn` -> `NewTypeRef`
- `TypeGenerics` -> `NewTypeRef`
- `TypeGroup` -> `NewTypeRef`
- `TypeImplTrait` -> `NewTypeRef`
- `TypeInfer` -> `NewTypeRef`
- `TypeMacro` -> `NewTypeRef`
- `TypeNever` -> `NewTypeRef`
- `TypeParam` -> `NewTypeRef`
- `TypeParen` -> `NewTypeRef`
- `TypePath` -> `NewTypeRef`
- `TypePtr` -> `NewTypeRef`
- `TypeReference` -> `NewTypeRef`
- `TypeSlice` -> `NewTypeRef`
- `TypeTraitObject` -> `NewTypeRef`
- `TypeTuple` -> `NewTypeRef`
- `UseGlob` -> `NewImport`
- `UseGroup` -> `NewImport`
- `UseName` -> `NewImport`
- `UsePath` -> `NewImport`
- `UseRename` -> `NewImport`
- `Variadic` -> `NewMethodParameterIn`
- `Variant` -> `NewMember`
- `VisRestricted` -> `NewModifier`
- `WhereClause` -> `NewTypeParameter`

# CPG Node

- `NewAnnotation`:
  - fullname
- `NewAnnotationLiteral`
- `NewAnnotationParameter`
- `NewAnnotationParameterAssign`
- `NewArrayInitializer`
- `NewBinding`:
  - methodfullname
- `NewBlock`
- `NewCall`
  - methodfullname
  - dispatchType
- `NewClosureBinding`:
  - evaluationStrategy
- `NewComment`: filename
- `NewConfigFile`
- `NewControlStructure`:
  - controlStructureType
- `NewDependency`
- `NewFieldIdentifier`: : No child
- `NewFile`
- `NewFinding`
- `NewIdentifier`
- `NewImport`: No child
- `NewJumpLabel`
- `NewJumpTarget`
- `NewKeyValuePair`
- `NewLiteral`
- `NewLocal`: No child
- `NewLocation`:
  - filename
  - methodfullname
- `NewMember`
- `NewMetaData`
- `NewMethod`:
  - Phải có con là 1 node `NewMethodReturn`, `NewMethodParameterIn`
  - filename
  - fullname
- `NewMethodParameterIn`:
  - evaluationStrategy
  - isVariadic
- `NewMethodParameterOut`:
  - evaluationStrategy
  - isVariadic
- `NewMethodRef`
  - methodfullname
- `NewMethodReturn`:
  - evaluationStrategy
- `NewModifier`:
  - modifierType
- `NewNamespace`
- `NewNamespaceBlock`:
  - filename
  - fullname
- `NewReturn`
- `NewTag`
- `NewTagNodePair`
- `NewTemplateDom`
- `NewType`
  - fullname
  - typeDeclFullName
- `NewTypeArgument`
- `NewTypeDecl`:
  - filename
  - fullName
- `NewTypeParameter`
- `NewTypeRef`: No child
- `NewUnknown`

# CPG Property

- `NAME`
  Name of represented object, e.g., method name (e.g. "run")

- `FULL_NAME`
  This is the fully-qualified name of an entity, e.g., the fully-qualified name of a method or type. The details of what constitutes a fully-qualified name are language specific. This field SHOULD be human readable.

- `CODE`
  This field holds the code snippet that the node represents.

- `METHOD_FULL_NAME`
  The FULL_NAME of a method. Used to link CALL and METHOD nodes. It is required to have exactly one METHOD node for each METHOD_FULL_NAME

- `EVALUATION_STRATEGY`
  For formal method input parameters, output parameters, and return parameters, this field holds the evaluation strategy, which is one of the following: 1) `BY_REFERENCE` indicates that the parameter is passed by reference, 2) `BY_VALUE` indicates that it is passed by value, that is, a copy is made, 3) `BY_SHARING` the parameter is a pointer/reference and it is shared with the caller/callee. While a copy of the pointer is made, a copy of the object that it points to is not made.

- `DISPATCH_TYPE`
  This field holds the dispatch type of a call, which is either `STATIC_DISPATCH` or `DYNAMIC_DISPATCH`. For statically dispatched method calls, the call target is known at compile time while for dynamically dispatched calls, it can only be determined at runtime as it may depend on the type of an object (as is the case for virtual method calls) or calculation of an offset.

- `ORDER`
  This integer indicates the position of the node among its siblings in the AST. The left-most child has an order of 0.

- `CONTROL_STRUCTURE_TYPE`
  The `CONTROL_STRUCTURE_TYPE` field indicates which kind of control structure a `CONTROL_STRUCTURE` node represents. The available types are the following: BREAK, CONTINUE, DO, WHILE, FOR, GOTO, IF, ELSE, TRY, THROW and SWITCH.

- `MODIFIER_TYPE`
  The modifier type is a free-form string. The following are known modifier types: `STATIC`, `PUBLIC`, `PROTECTED`, `PRIVATE`, `ABSTRACT`, `NATIVE`, `CONSTRUCTOR`, `VIRTUAL`

- `TYPE_DECL_FULL_NAME`
  The static type decl of a TYPE. This property is matched against the FULL_NAME of TYPE_DECL nodes. It is required to have exactly one TYPE_DECL for each different TYPE_DECL_FULL_NAME

- `TYPE_FULL_NAME`
  This field contains the fully-qualified static type name of the program construct represented by a node. It is the name of an instantiated type, e.g., `java.util.List<Integer>`, rather than `java.util.List[T]`. If the type cannot be determined, this field should be set to the empty string.

- `IS_VARIADIC`
  Specifies whether a parameter is the variadic argument handling parameter of a variadic method. Only one parameter of a method is allowed to have this property set to true.

- `SIGNATURE`
  The method signature encodes the types of parameters in a string. The string SHOULD be human readable and suitable for differentiating methods with different parameter types sufficiently to allow for resolving of function overloading. The present specification does not enforce a strict format for the signature, that is, it can be chosen by the frontend implementor to fit the source language.

- `FILENAME`
  The path of the source file this node was generated from, relative to the root path in the meta data node. This field must be set but may be set to the value `<unknown>` to indicate that no source file can be associated with the node, e.g., because the node represents an entity known to exist because it is referenced, but for which the file that is is declared in is unknown.

- `ROOT`
  The path to the root directory of the source/binary this CPG is generated from.

- `LANGUAGE`
  This field indicates which CPG language frontend generated the CPG. Frontend developers may freely choose a value that describes their frontend so long as it is not used by an existing frontend. Reserved values are to date: C, LLVM, GHIDRA, PHP.

# Error

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='ARGUMENT' with direction='IN' not supported by nodeType='ARRAY_INITIALIZER'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='ARGUMENT' with direction='IN' not supported by nodeType='METHOD'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='AST' with direction='OUT' not supported by nodeType='TYPE_PARAMETER'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='AST' with direction='OUT' not supported by nodeType='TYPE_REF'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='CONDITION' with direction='IN' not supported by nodeType='ARRAY_INITIALIZER'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='CONDITION' with direction='IN' not supported by nodeType='FIELD_IDENTIFIER'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='CONDITION' with direction='IN' not supported by nodeType='LOCAL'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='RECEIVER' with direction='IN' not supported by nodeType='FIELD_IDENTIFIER'

- Exception in thread "Writer" java.lang.RuntimeException: Edge with type='RECEIVER' with direction='IN' not supported by nodeType='LOCAL'

# AST with namespace stack, scope

- methodAst:

```
namespaceStack.push(newMethodNode)
scope.pushNewScope(newMethodNode)

scope.popScope()
namespaceStack.pop()
```

- receiver (this, self, ...)

```
newThisParameterNode(...)
scope.addToScope(name, (node, typeFullname))
```

# Special Path

- Lỗi path sai dẫn đến trỏ sai, phải fix

val IDENTIFIER_NODE = "IDENTIFIER_NODE"
val TYPE_REF_NODE = "TYPE_REF_NODE"
val METHOD_REF_NODE = "METHOD_REF_NODE"

- ExprCall - METHOD_REF_NODE, IDENTIFIER_NODE
- ExprPath - IDENTIFIER_NODE
- ExprStruct - TYPE_REF_NODE, IDENTIFIER_NODE, rest IDENTIFIER_NODE
- Field - TYPE_REF_NODE
- FieldPat - IDENTIFIER_NODE
- FieldValue - IDENTIFIER_NODE
- ItemImpl - TYPE_REF_NODE
- Macro - METHOD_REF_NODE
- MacroEmbeddable - METHOD_REF_NODE
- Meta - METHOD_REF_NODE
- MetaList - METHOD_REF_NODE
- MetaNameValue - METHOD_REF_NODE
- Path
- PathEmbeddable
- PatPath
- PatStruct - TYPE_REF_NODE
- PatTupleStruct - TYPE_REF_NODE
- TraitBound - TYPE_REF_NODE
- TypePath - TYPE_REF_NODE
- UsePath - Namespace
- VisibilityRestricted - TYPE_REF_NODE

# EdgeTypes

import io.shiftleft.codepropertygraph.generated.EdgeTypes
EdgeTypes.ALIAS_OF
EdgeTypes.ARGUMENT
EdgeTypes.AST
EdgeTypes.BINDS
EdgeTypes.CALL
EdgeTypes.CAPTURE
EdgeTypes.CDG
EdgeTypes.CFG
EdgeTypes.CONDITION
EdgeTypes.CONTAINS
EdgeTypes.DOMINATE
EdgeTypes.EVAL_TYPE
EdgeTypes.IMPORTS
EdgeTypes.INHERITS_FROM
EdgeTypes.IS_CALL_FOR_IMPORT
EdgeTypes.PARAMETER_LINK
EdgeTypes.POST_DOMINATE
EdgeTypes.REACHING_DEF
EdgeTypes.RECEIVER
EdgeTypes.REF
EdgeTypes.SOURCE_FILE

# Difference syntax in rust

- macro không lấy được AST mà các ký tự tồn tại dưới dạng token (literal). Xử lý bằng cách thử sử dụng `cargo-expand`

- Chưa xử lý được AngleBracketedGenericArguments, ParenthesizedGenericArguments là node con của Path
- Type ref hiện tại bị cụt, có thể cân nhắc trỏ đến toàn bộ cây của path
- Thử sửa lại path angle bracket để lấy được path ref đúng và generic argument
- Type parameter, Type argument, Type chưa ref được tới nhau
- Ref được type argument đến type parameter (không khả thi)
- Thử chuyển AstForType sang Type thay vì toàn bộ là TypeRef. Phân biệt rõ TypeDecl, Type
- Cân nhắc cắt bỏ toàn bộ `typeAst`, `patAst`, `exprAst`

- Chưa xử lý được kiểu `&Self` (không giải được) (có ý tưởng giải là trước đi đi vào thì set 1 cái biến &Self cụ thể là gì). Về cơ bản là giải quyết bài toán vào scope, vào name space, vào block
- Xử lý ident đặc biệt như `self`, `super`, `Self`

- Chưa có constructor cho Struct Unit, Struct Tuple, Struct Struct

- Chưa xử lý được bài toán vào module (mod)
- Gọi các module cùng file, khác file là chưa làm được. Scope làm được rồi nhưng module thì chưa (hay namespace khác nhau)

- Chưa xử lý được local (Xét 1 flag local)
- Kiểm tra lại mấy node `localNode()`
- Sửa lại methodParameterIn thành local node, ref được đến method parameter in

- Cân nhắc có cần phân biệt METHOD_REF, IDENTIFIER hay không. Chỉ cần để là identifier và sau đó ref đến

- Xem mẫu của code python, js, C để rút gọn các cạnh không cần thiết:

1. Method parameter in chỉ có 1 node duy nhất, không có các node con
2. Các biến sẽ ref tới method parameter in
3. Mỗi 1 method sẽ có [TYPE_DECL] (BINDS)-> [BINDING] (REF)-> [METHOD]
4. Có thể có nhiều modifier (public, static, abstract)
5. Constructor có modifier là CONSTRUCTOR
6. Operator không có AST của dấu
7. Field access là dùng phép call, Operator.fieldAccess
8. Generic class, class, struct, interface JS có memberN ode, nhưng memberNode đứng độc lập và phần code đứng độc lập. Nói chung memberNode thì tùy lựa chọn sử dụng
9. Generic trong Java,JS,C không sử dụng TypeParameter
10. Link TYPE_ARGUMENT và TYPE_PARAMETER (Java, C, không link)

11. Không sử dụng TYPE_REF mà sử dụng TYPE, khai báo riêng (Mỗi lần typeRef là tạo Type, và link đến)
12. Tạo thêm cạnh member, cạnh outlife, lifetime để chỉ có ra ý nghĩa của lifetime
