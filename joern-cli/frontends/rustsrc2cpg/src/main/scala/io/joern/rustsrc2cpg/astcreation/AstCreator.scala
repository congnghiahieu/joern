package io.joern.rustsrc2cpg.astcreation

import com.fasterxml.jackson.databind.ObjectMapper
import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.datastructures.Scope
import io.shiftleft.codepropertygraph.generated.nodes.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.shiftleft.codepropertygraph.generated.DiffGraphBuilder

import java.util
import scala.collection.mutable.{Map, Set, ListBuffer}
import java.nio.file.Paths

enum PathCPGNodeType {
  case IDENTIFIER_NODE
  // case FIELD_IDENTIFIER_NODE
  // case LITERAL_NODE
  case TYPEREF_NODE
  case METHODREF_NODE
}

class AstCreator(
  rootNode: FileAst,
  filePathCompareToCrate: String,
  cargoCrate: CargoCrate,
  protected var usedPrimitiveTypes: util.Set[String]
)(implicit val validationMode: ValidationMode)
    extends AstCreatorBase(filePathCompareToCrate)
    with AstForAbi
    with AstForArm
    with AstForAttribute
    with AstForExpr
    with CodeForExpr
    with AstForFields
    with CodeForFields
    with AstForFn
    with AstForFnArg
    with AstForForeignItem
    with AstForGenericArgument
    with CodeForGenericArgument
    with AstForGenericParam
    with AstForGenerics
    with AstForImplItem
    with AstForItem
    with AstForLit
    with AstForMacro
    with AstForMember
    with CodeForMember
    with AstForPat
    with CodeForPat
    with AstForOps
    with AstForMeta
    with CodeForMeta
    with AstForPathArguments
    with CodeForPathArguments
    with AstForRangeLimits
    with AstForStmt
    with AstForTokenTree
    with CodeForTokenTree
    with AstForTraitBoundModifier
    with AstForTraitItem
    with AstForType
    with AstForTypeParamBound
    with CodeForTypeParamBound
    with TypeFullnameForType
    with AstForUseTree
    with CodeForUseTree
    with AstForVisibility
    with AstForWherePredicate
    with AstNodeBuilder[RustAst, AstCreator] {

  protected val logger: Logger                                    = LoggerFactory.getLogger(classOf[AstCreator])
  protected val objectMapper: ObjectMapper                        = ObjectMapper()
  protected val namespaceStack: util.Stack[NewNode]               = new util.Stack()
  protected val namespaceMap: util.Map[String, NewNamespaceBlock] = new util.HashMap()
  protected var globalAst: Option[Ast]                            = None
  protected val scope: Scope[String, (NewNode, String), NewNode]  = new Scope()

  protected val primitiveTypeSet: Set[String] = Set(
    Primitives.BOOL,
    Primitives.CHAR,
    Primitives.STR,
    Primitives.U8,
    Primitives.U16,
    Primitives.U32,
    Primitives.U64,
    Primitives.U128,
    Primitives.USIZE,
    Primitives.I8,
    Primitives.I16,
    Primitives.I32,
    Primitives.I64,
    Primitives.I128,
    Primitives.ISIZE,
    Primitives.F32,
    Primitives.F64,
    Primitives.UNIT
  )
  protected val typeSet: Set[String]                    = Set().concat(primitiveTypeSet)
  protected val typeDeclMap: Map[String, NewTypeDecl]   = Map()
  protected val localNodeMap: Map[String, NewLocal]     = Map()
  protected val methodNodeMap: Map[String, NewMethod]   = Map()
  protected val typeNodeMap: Map[String, NewType]       = Map()
  protected var currentPathCpgNodeType: PathCPGNodeType = PathCPGNodeType.IDENTIFIER_NODE

  def createAst(): DiffGraphBuilder = {
    val ast = astForTranslationUnit(rootNode)
    Ast.storeInDiffGraph(ast, diffGraph)
    globalAst = Option(ast)
    diffGraph
  }

  private def astForTranslationUnit(root: FileAst): Ast = {

    val parentFullname = ""
    val namespaceBlock = NewNamespaceBlock()
      .name(filePathCompareToCrate)
      .filename(filePathCompareToCrate)
      .fullName(Paths.get(cargoCrate.cratePath, filePathCompareToCrate).toString)
    val namespaceAst = Ast(namespaceBlock)

    namespaceStack.push(namespaceAst.root.get)
    scope.pushNewScope(namespaceBlock)

    val annotationsAst = root.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filePathCompareToCrate, parentFullname, _)).toList
      case None        => List()
    }
    val itemAst = root.items.map(astForItem(filePathCompareToCrate, parentFullname, _)).toList

    scope.popScope()
    namespaceStack.pop()

    namespaceAst
      .withChildren(annotationsAst)
      .withChildren(itemAst)
  }

  def getCurrentPathCpgNodeType: PathCPGNodeType           = currentPathCpgNodeType
  def setCurrentPathCpgNodeType(nodeType: PathCPGNodeType) = currentPathCpgNodeType = nodeType

  //  TODO: Need implements correctly
  protected override def code(node: RustAst): String = ""

  protected override def column(node: RustAst): Option[Int] = Option(0)

  protected override def columnEnd(node: RustAst): Option[Int] = Option(0)

  protected override def line(node: RustAst): Option[Int] = Option(0)

  protected override def lineEnd(node: RustAst): Option[Int] = Option(0)
}
