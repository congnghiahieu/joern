package io.joern.rustsrc2cpg.astcreation

import com.fasterxml.jackson.databind.ObjectMapper
import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.datastructures.Scope
import io.shiftleft.codepropertygraph.generated.DiffGraphBuilder
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.types.structure.NamespaceTraversal
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Stack

enum PathCPGNodeType {
  case IDENTIFIER_NODE
  // case FIELD_IDENTIFIER_NODE
  // case LITERAL_NODE
  case TYPEREF_NODE
  case METHODREF_NODE
}

// Translates the Rust AST (Syn library) into a CPG AST.
class AstCreator(
  rootNode: FileAst,
  filePathCompareToCrate: String,
  cargoCrate: CargoCrate,
  protected var usedPrimitiveTypes: HashSet[String]
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
    with CodeForGenericParam
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

  protected val logger: Logger                                   = LoggerFactory.getLogger(classOf[AstCreator])
  protected val objectMapper: ObjectMapper                       = ObjectMapper()
  protected val namespaceStack: Stack[NewNode]                   = Stack.empty
  protected val namespaceMap: HashMap[String, NewNamespaceBlock] = HashMap.empty
  protected var globalAst: Option[Ast]                           = None
  protected val scope: Scope[String, (NewNode, String), NewNode] = new Scope()

  protected val primitiveTypeSet: HashSet[String] = HashSet(
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
  protected val typeSet: HashSet[String]                  = HashSet().concat(primitiveTypeSet)
  protected val typeDeclMap: HashMap[String, NewTypeDecl] = HashMap.empty
  protected val localNodeMap: HashMap[String, NewLocal]   = HashMap.empty
  protected val methodNodeMap: HashMap[String, NewMethod] = HashMap.empty
  protected val typeNodeMap: HashMap[String, NewType]     = HashMap.empty

  protected var currentPathCpgNodeType: PathCPGNodeType              = PathCPGNodeType.IDENTIFIER_NODE
  protected def getCurrentPathCpgNodeType: PathCPGNodeType           = currentPathCpgNodeType
  protected def setCurrentPathCpgNodeType(nodeType: PathCPGNodeType) = currentPathCpgNodeType = nodeType

  def createAst(): DiffGraphBuilder = {
    val fileNode = NewFile()
      .name(filePathCompareToCrate)
      .order(0)

    val ast = Ast(fileNode).withChild(astForTranslationUnit(rootNode))
    Ast.storeInDiffGraph(ast, diffGraph)
    diffGraph
  }

  private def astForTranslationUnit(root: FileAst): Ast = {
    val parentFullname = NamespaceTraversal.globalNamespaceName
    val namespaceBlock = NewNamespaceBlock()
      .name(filePathCompareToCrate.replaceAll("/", "::").replaceFirst(".rs", ""))
      .filename(filePathCompareToCrate)
      .fullName(Paths.get(cargoCrate.cratePath, filePathCompareToCrate).toString)

    namespaceStack.push(namespaceBlock)
    scope.pushNewScope(namespaceBlock)

    val annotationsAst = root.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filePathCompareToCrate, parentFullname, _)).toList
      case None        => List()
    }
    val itemAst      = root.items.map(astForItem(filePathCompareToCrate, parentFullname, _)).toList
    val childrenAsts = annotationsAst ++ itemAst
    setArgumentIndices(childrenAsts)

    scope.popScope()
    namespaceStack.pop()

    Ast(namespaceBlock).withChildren(childrenAsts)
  }

  //  TODO: Need implements correctly
  protected override def code(node: RustAst): String = ""

  protected override def column(node: RustAst): Option[Int] = Option(0)

  protected override def columnEnd(node: RustAst): Option[Int] = Option(0)

  protected override def line(node: RustAst): Option[Int] = Option(0)

  protected override def lineEnd(node: RustAst): Option[Int] = Option(0)
}
