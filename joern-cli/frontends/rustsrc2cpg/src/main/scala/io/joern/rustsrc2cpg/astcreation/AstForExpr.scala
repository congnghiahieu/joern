package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.{newOperatorCallNode}
import io.shiftleft.codepropertygraph.generated.{DispatchTypes, NodeTypes, Operators, PropertyNames}
import io.shiftleft.codepropertygraph.generated.ControlStructureTypes
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*

import scala.collection.mutable.ListBuffer

trait AstForExpr(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForExpr(filename: String, parentFullname: String, exprInstance: Expr): Ast = {
    if (exprInstance.arrayExpr.isDefined) {
      astForExprArray(filename, parentFullname, exprInstance.arrayExpr.get)
    } else if (exprInstance.assignExpr.isDefined) {
      astForExprAssign(filename, parentFullname, exprInstance.assignExpr.get)
    } else if (exprInstance.asyncExpr.isDefined) {
      astForExprAsync(filename, parentFullname, exprInstance.asyncExpr.get)
    } else if (exprInstance.awaitExpr.isDefined) {
      astForExprAwait(filename, parentFullname, exprInstance.awaitExpr.get)
    } else if (exprInstance.binaryExpr.isDefined) {
      astForExprBinary(filename, parentFullname, exprInstance.binaryExpr.get)
    } else if (exprInstance.blockExpr.isDefined) {
      astForExprBlock(filename, parentFullname, exprInstance.blockExpr.get)
    } else if (exprInstance.breakExpr.isDefined) {
      astForExprBreak(filename, parentFullname, exprInstance.breakExpr.get)
    } else if (exprInstance.callExpr.isDefined) {
      astForExprCall(filename, parentFullname, exprInstance.callExpr.get)
    } else if (exprInstance.castExpr.isDefined) {
      astForExprCast(filename, parentFullname, exprInstance.castExpr.get)
    } else if (exprInstance.closureExpr.isDefined) {
      astForExprClosure(filename, parentFullname, exprInstance.closureExpr.get)
    } else if (exprInstance.constExpr.isDefined) {
      astForExprConst(filename, parentFullname, exprInstance.constExpr.get)
    } else if (exprInstance.continueExpr.isDefined) {
      astForExprContinue(filename, parentFullname, exprInstance.continueExpr.get)
    } else if (exprInstance.fieldExpr.isDefined) {
      astForExprField(filename, parentFullname, exprInstance.fieldExpr.get)
    } else if (exprInstance.forLoopExpr.isDefined) {
      astForExprForLoop(filename, parentFullname, exprInstance.forLoopExpr.get)
    } else if (exprInstance.groupExpr.isDefined) {
      astForExprGroup(filename, parentFullname, exprInstance.groupExpr.get)
    } else if (exprInstance.ifExpr.isDefined) {
      astForExprIf(filename, parentFullname, exprInstance.ifExpr.get)
    } else if (exprInstance.indexExpr.isDefined) {
      astForExprIndex(filename, parentFullname, exprInstance.indexExpr.get)
    } else if (exprInstance.inferExpr.isDefined) {
      astForExprInfer(filename, parentFullname, exprInstance.inferExpr.get)
    } else if (exprInstance.letExpr.isDefined) {
      astForExprLet(filename, parentFullname, exprInstance.letExpr.get)
    } else if (exprInstance.litExpr.isDefined) {
      astForExprLit(filename, parentFullname, exprInstance.litExpr.get)
    } else if (exprInstance.loopExpr.isDefined) {
      astForExprLoop(filename, parentFullname, exprInstance.loopExpr.get)
    } else if (exprInstance.macroExpr.isDefined) {
      astForExprMacro(filename, parentFullname, exprInstance.macroExpr.get)
    } else if (exprInstance.matchExpr.isDefined) {
      astForExprMatch(filename, parentFullname, exprInstance.matchExpr.get)
    } else if (exprInstance.methodCallExpr.isDefined) {
      astForExprMethodCall(filename, parentFullname, exprInstance.methodCallExpr.get)
    } else if (exprInstance.parenExpr.isDefined) {
      astForExprParen(filename, parentFullname, exprInstance.parenExpr.get)
    } else if (exprInstance.pathExpr.isDefined) {
      astForExprPath(filename, parentFullname, exprInstance.pathExpr.get)
    } else if (exprInstance.rangeExpr.isDefined) {
      astForExprRange(filename, parentFullname, exprInstance.rangeExpr.get)
    } else if (exprInstance.referenceExpr.isDefined) {
      astForExprReference(filename, parentFullname, exprInstance.referenceExpr.get)
    } else if (exprInstance.repeatExpr.isDefined) {
      astForExprRepeat(filename, parentFullname, exprInstance.repeatExpr.get)
    } else if (exprInstance.returnExpr.isDefined) {
      astForExprReturn(filename, parentFullname, exprInstance.returnExpr.get)
    } else if (exprInstance.structExpr.isDefined) {
      astForExprStruct(filename, parentFullname, exprInstance.structExpr.get)
    } else if (exprInstance.tryExpr.isDefined) {
      astForExprTry(filename, parentFullname, exprInstance.tryExpr.get)
    } else if (exprInstance.tryBlockExpr.isDefined) {
      astForExprTryBlock(filename, parentFullname, exprInstance.tryBlockExpr.get)
    } else if (exprInstance.tupleExpr.isDefined) {
      astForExprTuple(filename, parentFullname, exprInstance.tupleExpr.get)
    } else if (exprInstance.unaryExpr.isDefined) {
      astForExprUnary(filename, parentFullname, exprInstance.unaryExpr.get)
    } else if (exprInstance.unsafeExpr.isDefined) {
      astForExprUnsafe(filename, parentFullname, exprInstance.unsafeExpr.get)
    } else if (exprInstance.verbatimExpr.isDefined) {
      astForTokenStream(filename, parentFullname, exprInstance.verbatimExpr.get)
    } else if (exprInstance.whileExpr.isDefined) {
      astForExprWhile(filename, parentFullname, exprInstance.whileExpr.get)
    } else if (exprInstance.yieldExpr.isDefined) {
      astForExprYield(filename, parentFullname, exprInstance.yieldExpr.get)
    } else {
      throw new IllegalArgumentException("Unsupported expression type")
    }
  }

  def astForExprArray(filename: String, parentFullname: String, arrayExprInstance: ExprArray): Ast = {
    val annotationsAst = arrayExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val elemsAst = arrayExprInstance.elems.map(astForExpr(filename, parentFullname, _)).toList
    val code     = codeForExprArray(filename, parentFullname, arrayExprInstance)

    val call = callNode(
      arrayExprInstance,
      code,
      Operators.arrayInitializer,
      Operators.arrayInitializer,
      DispatchTypes.STATIC_DISPATCH
    )
    callAst(call, elemsAst.toIndexedSeq)
      .withChildren(annotationsAst)
  }

  def astForExprAssign(filename: String, parentFullname: String, assignExprInstance: ExprAssign): Ast = {
    val annotationsAst = assignExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val leftAst = assignExprInstance.left match {
      case Some(left) => astForExpr(filename, parentFullname, left)
      case None       => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val rightAst = assignExprInstance.right match {
      case Some(right) => astForExpr(filename, parentFullname, right)
      case None        => Ast()
    }

    val code = codeForExprAssign(filename, parentFullname, assignExprInstance)
    val node = newOperatorCallNode(Operators.assignment, code)

    callAst(node, Seq(leftAst, rightAst))
      .withChildren(annotationsAst)
  }

  def astForExprAsync(filename: String, parentFullname: String, asyncExprInstance: ExprAsync): Ast = {
    val code      = codeForExprAsync(filename, parentFullname, asyncExprInstance)
    val asyncNode = blockNode(asyncExprInstance, code, "")

    scope.pushNewScope(asyncNode)
    val annotationsAst = asyncExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val stmtAst = asyncExprInstance.stmts.map(astForStmt(filename, parentFullname, _)).toList
    scope.popScope()

    blockAst(asyncNode, stmtAst)
      .withChildren(annotationsAst)
  }

  def astForExprAwait(filename: String, parentFullname: String, awaitExprInstance: ExprAwait): Ast = {
    val annotationsAst = awaitExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    val code     = codeForExprAwait(filename, parentFullname, awaitExprInstance)
    val awaitAst = callNode(awaitExprInstance, code, code, code, DispatchTypes.STATIC_DISPATCH, None, None)

    awaitExprInstance.base match {
      case Some(base) =>
        setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
        val baseAst = astForExpr(filename, parentFullname, base)
        callAst(awaitAst, Seq(), Some(baseAst))
          .withChildren(annotationsAst)
      case None =>
        callAst(awaitAst).withChildren(annotationsAst)
    }
  }

  def astForExprBinary(filename: String, parentFullname: String, binaryExprInstance: ExprBinary): Ast = {
    val annotationsAst = binaryExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val leftAst = binaryExprInstance.left match {
      case Some(left) => astForExpr(filename, parentFullname, left)
      case None       => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val rightAst = binaryExprInstance.right match {
      case Some(right) => astForExpr(filename, parentFullname, right)
      case None        => Ast()
    }

    val code      = codeForExprBinary(filename, parentFullname, binaryExprInstance)
    val operator  = BinOp.binOpToOperator(binaryExprInstance.op.get)
    val binaryAst = newOperatorCallNode(operator, code)

    callAst(binaryAst, Seq(leftAst, rightAst))
      .withChildren(annotationsAst)
  }

  def astForExprBlock(filename: String, parentFullname: String, blockExprInstance: ExprBlock): Ast = {
    val labelAst = blockExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    val code          = codeForExprBlock(filename, parentFullname, blockExprInstance)
    val exprBlockNode = blockNode(blockExprInstance, code, "")

    scope.pushNewScope(exprBlockNode)

    val annotationsAst = blockExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val stmtAst = blockExprInstance.stmts.map(astForStmt(filename, parentFullname, _)).toList

    scope.popScope()

    blockAst(exprBlockNode, stmtAst)
      .withChild(labelAst)
      .withChildren(annotationsAst)
  }

  def astForExprBreak(filename: String, parentFullname: String, breakExprInstance: ExprBreak): Ast = {
    val annotationsAst = breakExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    val labelAst = breakExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = breakExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val code          = codeForExprBreak(filename, parentFullname, breakExprInstance)
    val exprBreakNode = controlStructureNode(breakExprInstance, ControlStructureTypes.BREAK, code)

    controlStructureAst(exprBreakNode, None, Seq(labelAst, exprAst))
      .withChildren(annotationsAst)
  }

  def astForExprCall(filename: String, parentFullname: String, callExprInstance: ExprCall): Ast = {
    val annotationsAst = callExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.METHODREF_NODE)
    val funcAst = callExprInstance.func match {
      case Some(func) => astForExpr(filename, parentFullname, func)
      case None       => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val argsAst = callExprInstance.args.map(astForExpr(filename, parentFullname, _)).toList

    val code = codeForExprCall(filename, parentFullname, callExprInstance)
    val methodFullName = callExprInstance.func match {
      case Some(func) => codeForExpr(filename, parentFullname, func)
      case None       => Defines.Unknown
    }

    val callExprNode =
      callNode(callExprInstance, code, methodFullName, methodFullName, DispatchTypes.STATIC_DISPATCH, None, None)

    callAst(callExprNode, argsAst)
      .withChild(funcAst)
      .withChildren(annotationsAst)
  }

  def astForExprCast(filename: String, parentFullname: String, castExprInstance: ExprCast): Ast = {
    val annotationsAst = castExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = castExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val tyAst = castExprInstance.ty match {
      case Some(ty) => astForType(filename, parentFullname, ty)
      case None     => Ast()
    }

    val code = codeForExprCast(filename, parentFullname, castExprInstance)
    val node = newOperatorCallNode(Operators.cast, code)

    callAst(node, Seq(exprAst, tyAst))
      .withChildren(annotationsAst)
  }

  def astForExprClosure(filename: String, parentFullname: String, closureExprInstance: ExprClosure): Ast = {
    val closureFunctionName = nextClosureName()
    val closureNode =
      methodNode(
        closureExprInstance,
        closureFunctionName,
        "",
        closureFunctionName,
        None,
        filename,
        Some(NodeTypes.METHOD),
        None
      )

    scope.pushNewScope(closureNode)

    val annotationsAst = closureExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }
    val lifetimeAst =
      closureExprInstance.lifetimes match {
        case Some(lifetimes) => lifetimes.map(astForGenericParam(filename, parentFullname, _)).toSeq
        case None            => Seq()
      }
    val inputsAst = closureExprInstance.inputs.zipWithIndex.map { case (input, index) =>
      val inputAst = astForPat(filename, parentFullname, input)
      val wrapper  = parameterInNode(WrapperAst(), "", "", index, false, "", "")

      val inputCode = codeForPat(filename, parentFullname, input)
      scope.addToScope(inputCode, (wrapper, ""))

      Ast(wrapper).withChild(inputAst)
    }.toList
    val methodRetNode = closureExprInstance.output match {
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
    val bodyAst = closureExprInstance.body match {
      case Some(body) => astForExpr(filename, parentFullname, body)
      case None       => Ast()
    }

    scope.popScope()

    val methodAst = Ast(closureNode)
      .withChildren(inputsAst)
      .withChild(methodRetNode)
      .withChildren(annotationsAst)
      .withChild(bodyAst)
      .withChildren(lifetimeAst)
    Ast.storeInDiffGraph(methodAst, diffGraph)

    // Create type decl
    val closureTypeDecl =
      typeDeclNode(closureExprInstance, closureFunctionName, closureFunctionName, filename, "")
    closureTypeDecl.astParentFullName("").astParentType(NodeTypes.METHOD)
    Ast.storeInDiffGraph(Ast(closureTypeDecl), diffGraph)

    // Create method ref
    val node = methodRefNode(closureExprInstance, "", closureFunctionName, closureFunctionName)
    Ast(node)
      .withRefEdge(node, closureNode)
  }

  def astForExprConst(filename: String, parentFullname: String, constExprInstance: ExprConst): Ast = {
    val code      = codeForExprConst(filename, parentFullname, constExprInstance)
    val constNode = blockNode(constExprInstance, code, "")

    scope.pushNewScope(constNode)

    val annotationsAst = constExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val stmtsAst = constExprInstance.stmts.map(astForStmt(filename, parentFullname, _)).toList

    scope.popScope()

    blockAst(constNode, stmtsAst)
      .withChildren(annotationsAst)
  }

  def astForExprContinue(filename: String, parentFullname: String, continueExprInstance: ExprContinue): Ast = {
    val annotationsAst = continueExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toSeq
      case None        => Seq()
    }
    val labelAst = continueExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    val code             = codeForExprContinue(filename, parentFullname, continueExprInstance)
    val exprContinueNode = controlStructureNode(continueExprInstance, ControlStructureTypes.CONTINUE, code)

    controlStructureAst(exprContinueNode, None, Seq(labelAst))
      .withChildren(annotationsAst)
  }

  def astForExprField(filename: String, parentFullname: String, fieldExprInstance: ExprField): Ast = {
    val annotationsAst = fieldExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val baseAst = fieldExprInstance.base match {
      case Some(base) => astForExpr(filename, parentFullname, base)
      case None       => Ast()
    }

    val code     = codeForExprField(filename, parentFullname, fieldExprInstance)
    val ident    = code.split('.').last
    val fieldAst = Ast(fieldIdentifierNode(fieldExprInstance, ident, ident))

    val node = newOperatorCallNode(Operators.fieldAccess, code)

    callAst(node, Seq(baseAst, fieldAst))
      .withChildren(annotationsAst)
  }

  def astForExprForLoop(filename: String, parentFullname: String, forLoopExprInstance: ExprForLoop): Ast = {
    val labelAst = forLoopExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    val code        = codeForExprForLoop(filename, parentFullname, forLoopExprInstance)
    val forLoopNode = controlStructureNode(forLoopExprInstance, ControlStructureTypes.FOR, code)

    scope.pushNewScope(forLoopNode)

    val annotationsAst = forLoopExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val patAst = forLoopExprInstance.pat match {
      case Some(pat) => astForPat(filename, parentFullname, pat)
      case None      => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = forLoopExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val bodyAst = astForBlock(filename, parentFullname, forLoopExprInstance.body)

    scope.popScope()

    forAst(forLoopNode, Seq(patAst), Seq(), Seq(exprAst), Seq(), bodyAst)
      .withChild(labelAst)
      .withChildren(annotationsAst)
  }

  def astForExprGroup(filename: String, parentFullname: String, groupExprInstance: ExprGroup): Ast = {
    val annotationsAst = groupExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = groupExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    Ast(unknownNode(groupExprInstance, ""))
      .withChild(exprAst)
      .withChildren(annotationsAst)
  }

  def astForExprIf(filename: String, parentFullname: String, ifExprInstance: ExprIf): Ast = {
    val code       = codeForExprIf(filename, parentFullname, ifExprInstance)
    val exprIfNode = controlStructureNode(ifExprInstance, ControlStructureTypes.IF, code)

    scope.pushNewScope(exprIfNode)

    val annotationsAst = ifExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val condAst = ifExprInstance.cond match {
      case Some(cond) => astForExpr(filename, parentFullname, cond)
      case None       => Ast()
    }
    val thenAst = astForBlock(filename, parentFullname, ifExprInstance.then_branch)

    scope.popScope()

    scope.pushNewScope(blockNode(UnknownAst()))

    val elseAst = ifExprInstance.else_branch match {
      case Some(elseBranch) => {
        val exprAst  = astForExpr(filename, parentFullname, elseBranch)
        val code     = "else"
        val elseNode = controlStructureNode(ExprElse(), ControlStructureTypes.ELSE, code)
        controlStructureAst(elseNode, None, Seq(exprAst))
      }
      case None => Ast()
    }

    scope.popScope()

    controlStructureAst(exprIfNode, Some(condAst), Seq(thenAst, elseAst))
      .withChildren(annotationsAst)
  }

  def astForExprIndex(filename: String, parentFullname: String, indexExprInstance: ExprIndex): Ast = {
    val annotationsAst = indexExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = indexExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val indexAst = indexExprInstance.index match {
      case Some(index) => astForExpr(filename, parentFullname, index)
      case None        => Ast()
    }
    val code = codeForExprIndex(filename, parentFullname, indexExprInstance)

    val node = callNode(
      indexExprInstance,
      code,
      Operators.indirectIndexAccess,
      Operators.indirectIndexAccess,
      DispatchTypes.STATIC_DISPATCH
    )
    callAst(node, Seq(exprAst, indexAst))
      .withChildren(annotationsAst)
  }

  def astForExprInfer(filename: String, parentFullname: String, inferExprInstance: ExprInfer): Ast = {
    val annotationsAst = inferExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val code = codeForExprInfer(filename, parentFullname, inferExprInstance)
    val identNode =
      identifierNode(inferExprInstance, code, code, "")

    Ast(unknownNode(inferExprInstance, ""))
      .withChild(Ast(identNode))
      .withChildren(annotationsAst)
  }

  def astForExprLet(filename: String, parentFullname: String, letExprInstance: ExprLet): Ast = {
    val (lhsCode, typeFullname) = letExprInstance.pat match {
      case Some(pat) => extractCodeForPatType(codeForPat(filename, parentFullname, pat))
      case None      => (Defines.Unknown, Defines.Unknown)
    }
    // remove subPat, mut and ref (see class PatIdent)
    val identOnly = lhsCode.split("@").head.replace("mut", "").replace("ref", "").trim
    val localCode = s"let $lhsCode"
    val letNode   = localNode(letExprInstance, identOnly, localCode, typeFullname)
    scope.addToScope(identOnly, (letNode, localCode))

    val annotationsAst = letExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val patAst = letExprInstance.pat match {
      case Some(pat) => astForPat(filename, parentFullname, pat)
      case None      => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = letExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val fullCode       = codeForExprLet(filename, parentFullname, letExprInstance)
    val assignmentNode = newOperatorCallNode(Operators.assignment, fullCode)

    callAst(assignmentNode, Seq(patAst, exprAst))
      .withChild(Ast(letNode))
      .withChildren(annotationsAst)
  }

  def astForExprLit(filename: String, parentFullname: String, litExprInstance: ExprLit): Ast = {
    val annotationsAst = litExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val litInstance = Lit(
      litExprInstance.strLit,
      litExprInstance.byteStrLit,
      litExprInstance.byteLit,
      litExprInstance.charLit,
      litExprInstance.intLit,
      litExprInstance.floatLit,
      litExprInstance.boolLit,
      litExprInstance.verbatimLit
    )
    val exprLitAst = astForLit(filename, parentFullname, litInstance)

    exprLitAst
      .withChildren(annotationsAst)
  }

  def astForExprLoop(filename: String, parentFullname: String, loopExprInstance: ExprLoop): Ast = {
    val labelAst = loopExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    val code     = codeForExprLoop(filename, parentFullname, loopExprInstance)
    val loopNode = controlStructureNode(loopExprInstance, "LOOP", code)

    scope.pushNewScope(loopNode)

    val annotationsAst = loopExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val bodyAst = astForBlock(filename, parentFullname, loopExprInstance.body)

    scope.popScope()

    controlStructureAst(loopNode, None, Seq(labelAst, bodyAst))
      .withChildren(annotationsAst)
  }

  def astForExprMacro(filename: String, parentFullname: String, macroExprInstance: ExprMacro): Ast = {
    val annotationsAst = macroExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val macroRustAst = Macro(macroExprInstance.path, macroExprInstance.delimiter, macroExprInstance.tokens)
    astForMacro(filename, parentFullname, macroRustAst).withChildren(annotationsAst)
  }

  def astForExprMatch(filename: String, parentFullname: String, matchExprInstance: ExprMatch): Ast = {
    val annotationsAst = matchExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = matchExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val armsAst = matchExprInstance.arms.map(astForArm(filename, parentFullname, _)).toList

    val code         = codeForExprMatch(filename, parentFullname, matchExprInstance)
    val exprMatchAst = controlStructureNode(matchExprInstance, ControlStructureTypes.MATCH, code)

    controlStructureAst(exprMatchAst, Some(exprAst), armsAst)
      .withChildren(annotationsAst)
  }

  def astForExprMethodCall(filename: String, parentFullname: String, methodCallExprInstance: ExprMethodCall): Ast = {
    val annotationsAst = methodCallExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val baseAst = methodCallExprInstance.receiver match {
      case Some(receiver) => astForExpr(filename, parentFullname, receiver)
      case None           => Ast()
    }

    val turbofishAst = methodCallExprInstance.turbofish match {
      case Some(turbofish) => astForAngleBracketedGenericArguments(filename, parentFullname, turbofish)
      case None            => Ast()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val argsAst = methodCallExprInstance.args.map(astForExpr(filename, parentFullname, _)).toList

    val exprMethodCallAst = callNode(
      methodCallExprInstance,
      "",
      methodCallExprInstance.method,
      methodCallExprInstance.method,
      DispatchTypes.STATIC_DISPATCH,
      None,
      None
    )

    callAst(exprMethodCallAst, argsAst, Some(baseAst), None)
      .withChild(turbofishAst)
      .withChildren(annotationsAst)
  }

  def astForExprParen(filename: String, parentFullname: String, parenExprInstance: ExprParen): Ast = {
    val annotationsAst = parenExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val exprAst = parenExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val code          = codeForExprParen(filename, parentFullname, parenExprInstance)
    val exprParenNode = unknownNode(parenExprInstance, code)

    Ast(exprParenNode)
      .withChild(exprAst)
      .withChildren(annotationsAst)
  }

  def astForExprPath(filename: String, parentFullname: String, pathExprInstance: ExprPath): Ast = {
    val annotationsAst = pathExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    // Can not determine the type of the path is METHOD_REF or IDENTIFIER
    val path    = Path(pathExprInstance.segments, pathExprInstance.leading_colon)
    val pathAst = astForPath(filename, parentFullname, path, pathExprInstance.qself)

    pathAst
  }

  def astForExprRange(filename: String, parentFullname: String, rangeExprInstance: ExprRange): Ast = {
    val annotationsAst = rangeExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val startAst = rangeExprInstance.start match {
      case Some(start) => astForExpr(filename, parentFullname, start)
      case None        => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val endAst = rangeExprInstance.end match {
      case Some(end) => astForExpr(filename, parentFullname, end)
      case None      => Ast()
    }

    val code          = codeForExprRange(filename, parentFullname, rangeExprInstance)
    val exprRangeNode = newOperatorCallNode(Operators.range, code)

    callAst(exprRangeNode, Seq(startAst, endAst))
      .withChildren(annotationsAst)
  }

  def astForExprReference(filename: String, parentFullname: String, referenceExprInstance: ExprReference): Ast = {
    val annotationsAst = referenceExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val exprAst = referenceExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val code = codeForExprReference(filename, parentFullname, referenceExprInstance)
    val node = newOperatorCallNode(Operators.addressOf, code)

    callAst(node, Seq(exprAst))
      .withChildren(annotationsAst)
  }

  def astForExprRepeat(filename: String, parentFullname: String, repeatExprInstance: ExprRepeat): Ast = {
    val annotationsAst = repeatExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = repeatExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val lenAst = repeatExprInstance.len match {
      case Some(len) => astForExpr(filename, parentFullname, len)
      case None      => Ast()
    }
    val code = codeForExprRepeat(filename, parentFullname, repeatExprInstance)

    // val exprRepeatNode = NewArrayInitializer().code(code)
    // val exprRepeateAst = Ast(exprRepeatNode)
    //   .withChild(exprAst)
    //   .withChild(lenAst)
    //   .withChildren(annotationsAst)
    // Ast(unknownNode(repeatExprInstance, "")).withChild(exprRepeateAst)
    val call = callNode(
      repeatExprInstance,
      code,
      Operators.arrayInitializer,
      Operators.arrayInitializer,
      DispatchTypes.STATIC_DISPATCH
    )
    callAst(call, Seq(exprAst, lenAst))
      .withChildren(annotationsAst)
  }

  def astForExprReturn(filename: String, parentFullname: String, returnExprInstance: ExprReturn): Ast = {
    val annotationsAst = returnExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val exprAst = returnExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val code           = codeForExprReturn(filename, parentFullname, returnExprInstance)
    val exprReturnNode = returnNode(returnExprInstance, code)

    returnAst(exprReturnNode, Seq(exprAst))
      .withChildren(annotationsAst)
  }

  def astForExprStruct(filename: String, parentFullname: String, structExprInstance: ExprStruct): Ast = {
    val code          = codeForExprStruct(filename, parentFullname, structExprInstance)
    val exprStructAst = unknownNode(structExprInstance, code)

    scope.pushNewScope(exprStructAst)

    val annotationsAst = structExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.TYPEREF_NODE)
    val pathAst = structExprInstance.path match {
      case Some(path) => astForPath(filename, parentFullname, path, structExprInstance.qself)
      case None       => Ast()
    }
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val fieldsAst = structExprInstance.fields.map(astForFieldValue(filename, parentFullname, _)).toList
    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val restAst = structExprInstance.rest match {
      case Some(rest) => astForExpr(filename, parentFullname, rest)
      case None       => Ast()
    }

    scope.popScope()

    Ast(exprStructAst)
      .withChildren(annotationsAst)
      .withChild(pathAst)
      .withChildren(fieldsAst)
      .withChild(restAst)
  }

  def astForExprTry(filename: String, parentFullname: String, tryExprInstance: ExprTry): Ast = {
    val annotationsAst = tryExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val exprAst = tryExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val code       = codeForExprTry(filename, parentFullname, tryExprInstance)
    val exprTryAst = controlStructureNode(tryExprInstance, ControlStructureTypes.TRY, code)

    controlStructureAst(exprTryAst, Some(exprAst))
      .withChildren(annotationsAst)
  }

  def astForExprTryBlock(filename: String, parentFullname: String, tryBlockExprInstance: ExprTryBlock): Ast = {
    val code            = codeForExprTryBlock(filename, parentFullname, tryBlockExprInstance)
    val exprTryBlockAst = blockNode(tryBlockExprInstance, code, "")

    scope.pushNewScope(exprTryBlockAst)

    val annotationsAst = tryBlockExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val stmtsAst = tryBlockExprInstance.stmts.map(astForStmt(filename, parentFullname, _)).toList

    scope.popScope()

    blockAst(exprTryBlockAst, stmtsAst)
      .withChildren(annotationsAst)
  }

  def astForExprTuple(filename: String, parentFullname: String, tupleExprInstance: ExprTuple): Ast = {
    val annotationsAst = tupleExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val elemsAst = tupleExprInstance.elems.map(astForExpr(filename, parentFullname, _)).toList
    val code     = codeForExprTuple(filename, parentFullname, tupleExprInstance)

    val call = callNode(
      tupleExprInstance,
      code,
      Operators.arrayInitializer,
      Operators.arrayInitializer,
      DispatchTypes.STATIC_DISPATCH
    )
    callAst(call, elemsAst.toIndexedSeq)
      .withChildren(annotationsAst)
  }

  def astForExprUnary(filename: String, parentFullname: String, unaryExprInstance: ExprUnary): Ast = {
    val annotationsAst = unaryExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    setCurrentPathCpgNodeType(PathCPGNodeType.IDENTIFIER_NODE)
    val exprAst = unaryExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }

    val code     = codeForExprUnary(filename, parentFullname, unaryExprInstance)
    val operator = UnOp.unOpToOperator(unaryExprInstance.op.get)

    val exprUnaryNode = newOperatorCallNode(operator, code)
    callAst(exprUnaryNode, Seq(exprAst))
      .withChildren(annotationsAst)
  }

  def astForExprUnsafe(filename: String, parentFullname: String, unsafeExprInstance: ExprUnsafe): Ast = {
    val code          = codeForExprUnsafe(filename, parentFullname, unsafeExprInstance)
    val exprUnsafeAst = blockNode(unsafeExprInstance, code, "")

    scope.pushNewScope(exprUnsafeAst)

    val annotationsAst = unsafeExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val stmtsAst = unsafeExprInstance.stmts.map(astForStmt(filename, parentFullname, _)).toList

    scope.popScope()

    blockAst(exprUnsafeAst, stmtsAst)
      .withChildren(annotationsAst)
  }

  def astForExprWhile(filename: String, parentFullname: String, whileExprInstance: ExprWhile): Ast = {
    val labelAst = whileExprInstance.label match {
      case Some(label) => astForLabel(filename, parentFullname, label)
      case None        => Ast()
    }

    scope.pushNewScope(unknownNode(UnknownAst(), ""))

    val annotationsAst = whileExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }
    val condAst = whileExprInstance.cond match {
      case Some(cond) => astForExpr(filename, parentFullname, cond)
      case None       => Ast()
    }
    val bodyAst = astForBlock(filename, parentFullname, whileExprInstance.body)

    scope.popScope()

    val code = "while"
    whileAst(Some(condAst), Seq(bodyAst), Some(code))
      .withChild(labelAst)
      .withChildren(annotationsAst)
  }

  def astForExprYield(filename: String, parentFullname: String, yieldExprInstance: ExprYield): Ast = {
    val annotationsAst = yieldExprInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val exprAst = yieldExprInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val code         = codeForExprYield(filename, parentFullname, yieldExprInstance)
    val exprYieldAst = returnNode(yieldExprInstance, code)

    returnAst(exprYieldAst, Seq(exprAst))
      .withChildren(annotationsAst)
  }
}

trait CodeForExpr(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def codeForExpr(filename: String, parentFullname: String, exprInstance: Expr): String = {
    if (exprInstance.arrayExpr.isDefined) {
      codeForExprArray(filename, parentFullname, exprInstance.arrayExpr.get)
    } else if (exprInstance.assignExpr.isDefined) {
      codeForExprAssign(filename, parentFullname, exprInstance.assignExpr.get)
    } else if (exprInstance.asyncExpr.isDefined) {
      codeForExprAsync(filename, parentFullname, exprInstance.asyncExpr.get)
    } else if (exprInstance.awaitExpr.isDefined) {
      codeForExprAwait(filename, parentFullname, exprInstance.awaitExpr.get)
    } else if (exprInstance.binaryExpr.isDefined) {
      codeForExprBinary(filename, parentFullname, exprInstance.binaryExpr.get)
    } else if (exprInstance.blockExpr.isDefined) {
      codeForExprBlock(filename, parentFullname, exprInstance.blockExpr.get)
    } else if (exprInstance.breakExpr.isDefined) {
      codeForExprBreak(filename, parentFullname, exprInstance.breakExpr.get)
    } else if (exprInstance.callExpr.isDefined) {
      codeForExprCall(filename, parentFullname, exprInstance.callExpr.get)
    } else if (exprInstance.castExpr.isDefined) {
      codeForExprCast(filename, parentFullname, exprInstance.castExpr.get)
    } else if (exprInstance.closureExpr.isDefined) {
      codeForExprClosure(filename, parentFullname, exprInstance.closureExpr.get)
    } else if (exprInstance.constExpr.isDefined) {
      codeForExprConst(filename, parentFullname, exprInstance.constExpr.get)
    } else if (exprInstance.continueExpr.isDefined) {
      codeForExprContinue(filename, parentFullname, exprInstance.continueExpr.get)
    } else if (exprInstance.fieldExpr.isDefined) {
      codeForExprField(filename, parentFullname, exprInstance.fieldExpr.get)
    } else if (exprInstance.forLoopExpr.isDefined) {
      codeForExprForLoop(filename, parentFullname, exprInstance.forLoopExpr.get)
    } else if (exprInstance.groupExpr.isDefined) {
      codeForExprGroup(filename, parentFullname, exprInstance.groupExpr.get)
    } else if (exprInstance.ifExpr.isDefined) {
      codeForExprIf(filename, parentFullname, exprInstance.ifExpr.get)
    } else if (exprInstance.indexExpr.isDefined) {
      codeForExprIndex(filename, parentFullname, exprInstance.indexExpr.get)
    } else if (exprInstance.inferExpr.isDefined) {
      codeForExprInfer(filename, parentFullname, exprInstance.inferExpr.get)
    } else if (exprInstance.letExpr.isDefined) {
      codeForExprLet(filename, parentFullname, exprInstance.letExpr.get)
    } else if (exprInstance.litExpr.isDefined) {
      codeForExprLit(filename, parentFullname, exprInstance.litExpr.get)
    } else if (exprInstance.loopExpr.isDefined) {
      codeForExprLoop(filename, parentFullname, exprInstance.loopExpr.get)
    } else if (exprInstance.macroExpr.isDefined) {
      codeForExprMacro(filename, parentFullname, exprInstance.macroExpr.get)
    } else if (exprInstance.matchExpr.isDefined) {
      codeForExprMatch(filename, parentFullname, exprInstance.matchExpr.get)
    } else if (exprInstance.methodCallExpr.isDefined) {
      codeForExprMethodCall(filename, parentFullname, exprInstance.methodCallExpr.get)
    } else if (exprInstance.parenExpr.isDefined) {
      codeForExprParen(filename, parentFullname, exprInstance.parenExpr.get)
    } else if (exprInstance.pathExpr.isDefined) {
      codeForExprPath(filename, parentFullname, exprInstance.pathExpr.get)
    } else if (exprInstance.rangeExpr.isDefined) {
      codeForExprRange(filename, parentFullname, exprInstance.rangeExpr.get)
    } else if (exprInstance.referenceExpr.isDefined) {
      codeForExprReference(filename, parentFullname, exprInstance.referenceExpr.get)
    } else if (exprInstance.repeatExpr.isDefined) {
      codeForExprRepeat(filename, parentFullname, exprInstance.repeatExpr.get)
    } else if (exprInstance.returnExpr.isDefined) {
      codeForExprReturn(filename, parentFullname, exprInstance.returnExpr.get)
    } else if (exprInstance.structExpr.isDefined) {
      codeForExprStruct(filename, parentFullname, exprInstance.structExpr.get)
    } else if (exprInstance.tryExpr.isDefined) {
      codeForExprTry(filename, parentFullname, exprInstance.tryExpr.get)
    } else if (exprInstance.tryBlockExpr.isDefined) {
      codeForExprTryBlock(filename, parentFullname, exprInstance.tryBlockExpr.get)
    } else if (exprInstance.tupleExpr.isDefined) {
      codeForExprTuple(filename, parentFullname, exprInstance.tupleExpr.get)
    } else if (exprInstance.unaryExpr.isDefined) {
      codeForExprUnary(filename, parentFullname, exprInstance.unaryExpr.get)
    } else if (exprInstance.unsafeExpr.isDefined) {
      codeForExprUnsafe(filename, parentFullname, exprInstance.unsafeExpr.get)
    } else if (exprInstance.verbatimExpr.isDefined) {
      codeForTokenStream(filename, parentFullname, exprInstance.verbatimExpr.get)
    } else if (exprInstance.whileExpr.isDefined) {
      codeForExprWhile(filename, parentFullname, exprInstance.whileExpr.get)
    } else if (exprInstance.yieldExpr.isDefined) {
      codeForExprYield(filename, parentFullname, exprInstance.yieldExpr.get)
    } else {
      throw new IllegalArgumentException("Unsupported expression type")
    }
  }

  def codeForExprArray(filename: String, parentFullname: String, arrayExprInstance: ExprArray): String = {
    s"[${arrayExprInstance.elems.map(codeForExpr(filename, parentFullname, _)).mkString(", ")}]"
  }
  def codeForExprAssign(filename: String, parentFullname: String, assignExprInstance: ExprAssign): String = {
    val leftCode = assignExprInstance.left match {
      case Some(left) => codeForExpr(filename, parentFullname, left)
      case None       => Defines.Unknown
    }
    val rightCode = assignExprInstance.right match {
      case Some(right) => codeForExpr(filename, parentFullname, right)
      case None        => Defines.Unknown
    }
    s"$leftCode = $rightCode"
  }
  def codeForExprAsync(filename: String, parentFullname: String, asyncExprInstance: ExprAsync): String = {
    val blockCode = codeForBlock(filename, parentFullname, asyncExprInstance.stmts)
    val code = asyncExprInstance.move match {
      case Some(true) => s"async move {$blockCode}"
      case _          => s"async {$blockCode}"
    }
    code
  }
  def codeForExprAwait(filename: String, parentFullname: String, awaitExprInstance: ExprAwait): String = {
    ".await"
  }
  def codeForExprBinary(filename: String, parentFullname: String, binaryExprInstance: ExprBinary): String = {

    val leftCode = binaryExprInstance.left match {
      case Some(left) => codeForExpr(filename, parentFullname, left)
      case None       => Defines.Unknown
    }
    val rightCode = binaryExprInstance.right match {
      case Some(right) => codeForExpr(filename, parentFullname, right)
      case None        => Defines.Unknown
    }
    val opCode = binaryExprInstance.op match {
      case Some(op) => op.toString
      case None     => Defines.Unknown
    }
    s"$leftCode $opCode $rightCode"
  }
  def codeForExprBlock(filename: String, parentFullname: String, blockExprInstance: ExprBlock): String = {
    var blockCode = codeForBlock(filename, parentFullname, blockExprInstance.stmts)
    blockExprInstance.label match {
      case Some(label) => s"'${label}: $blockCode"
      case None        => blockCode
    }
  }
  def codeForExprBreak(filename: String, parentFullname: String, breakExprInstance: ExprBreak): String = {
    var code = "break"
    code = breakExprInstance.expr match {
      case Some(expr) => s"$code ${codeForExpr(filename, parentFullname, expr)}"
      case None       => code
    }
    breakExprInstance.label match {
      case Some(label) => s"break '${label}"
      case None        => code
    }
  }
  def codeForExprCall(filename: String, parentFullname: String, callExprInstance: ExprCall): String = {
    val funcCode = callExprInstance.func match {
      case Some(callee) => codeForExpr(filename, parentFullname, callee)
      case None         => Defines.Unknown
    }
    val argsCode = callExprInstance.args.map(codeForExpr(filename, parentFullname, _)).mkString(", ")
    s"$funcCode($argsCode)"
  }
  def codeForExprCast(filename: String, parentFullname: String, castExprInstance: ExprCast): String = {
    var code = ""
    code = castExprInstance.expr match {
      case Some(expr) => s"${codeForExpr(filename, parentFullname, expr)}"
      case None       => Defines.Unknown
    }
    castExprInstance.ty match {
      case Some(ty) => s"$code as ${typeFullnameForType(filename, parentFullname, ty)}"
      case None     => code
    }
  }
  def codeForExprClosure(filename: String, parentFullname: String, closureExprInstance: ExprClosure): String = {
    "ClosureExpr"
  }
  def codeForExprConst(filename: String, parentFullname: String, constExprInstance: ExprConst): String = {
    "const {}"
  }
  def codeForExprContinue(filename: String, parentFullname: String, continueExprInstance: ExprContinue): String = {
    continueExprInstance.label match {
      case Some(label) => s"continue '${label}"
      case None        => "continue"
    }
  }
  def codeForExprField(filename: String, parentFullname: String, fieldExprInstance: ExprField): String = {
    val baseName = fieldExprInstance.base match {
      case Some(base) => codeForExpr(filename, parentFullname, base)
      case None       => Defines.Unknown
    }
    var ident = fieldExprInstance.named match {
      case Some(name) => name
      case None       => Defines.Unknown
    }
    ident = fieldExprInstance.unnamed match {
      case Some(index) => index.toString
      case None        => ident
    }
    s"$baseName.$ident"
  }
  def codeForExprForLoop(filename: String, parentFullname: String, forLoopExprInstance: ExprForLoop): String = {
    "for"
  }
  def codeForExprGroup(filename: String, parentFullname: String, groupExprInstance: ExprGroup): String = {
    "GroupExpr"
  }
  def codeForExprIf(filename: String, parentFullname: String, ifExprInstance: ExprIf): String = {
    "if"
  }
  def codeForExprIndex(filename: String, parentFullname: String, indexExprInstance: ExprIndex): String = {
    val exprCode = indexExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val indexCode = indexExprInstance.index match {
      case Some(index) => codeForExpr(filename, parentFullname, index)
      case None        => Defines.Unknown
    }
    s"$exprCode[$indexCode]"
  }
  def codeForExprInfer(filename: String, parentFullname: String, inferExprInstance: ExprInfer): String = {
    "_"
  }
  def codeForExprLet(filename: String, parentFullname: String, letExprInstance: ExprLet): String = {
    val patCode = letExprInstance.pat match {
      case Some(pat) => codeForPat(filename, parentFullname, pat)
      case None      => Defines.Unknown
    }
    val exprCode = letExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    s"let $patCode = $exprCode"
  }
  def codeForExprLit(filename: String, parentFullname: String, litExprInstance: ExprLit): String = {
    val litInstance = Lit(
      litExprInstance.strLit,
      litExprInstance.byteStrLit,
      litExprInstance.byteLit,
      litExprInstance.charLit,
      litExprInstance.intLit,
      litExprInstance.floatLit,
      litExprInstance.boolLit,
      litExprInstance.verbatimLit
    )
    codeForLit(filename, parentFullname, litInstance)
  }
  def codeForExprLoop(filename: String, parentFullname: String, loopExprInstance: ExprLoop): String = {
    "loop"
  }
  def codeForExprMacro(filename: String, parentFullname: String, macroExprInstance: ExprMacro): String = {
    val macroRustAst = Macro(macroExprInstance.path, macroExprInstance.delimiter, macroExprInstance.tokens)
    val (_, _, code) = codeForMacro(filename, parentFullname, macroRustAst)
    code
  }
  def codeForExprMatch(filename: String, parentFullname: String, matchExprInstance: ExprMatch): String = {
    "match"
  }
  def codeForExprMethodCall(
    filename: String,
    parentFullname: String,
    methodCallExprInstance: ExprMethodCall
  ): String = {
    "MethodCallExpr"
  }
  def codeForExprParen(filename: String, parentFullname: String, parenExprInstance: ExprParen): String = {
    val exprCode = parenExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    s"($exprCode)"
  }
  def codeForExprPath(filename: String, parentFullname: String, pathExprInstance: ExprPath): String = {
    val path         = Path(pathExprInstance.segments, pathExprInstance.leading_colon)
    val typeFullname = typeFullnameForPath(filename, parentFullname, path, pathExprInstance.qself)
    typeFullname
  }
  def codeForExprRange(filename: String, parentFullname: String, rangeExprInstance: ExprRange): String = {
    val startCode = rangeExprInstance.start match {
      case Some(start) => codeForExpr(filename, parentFullname, start)
      case None        => ""
    }
    val endCode = rangeExprInstance.end match {
      case Some(end) => codeForExpr(filename, parentFullname, end)
      case None      => ""
    }
    val limitCode = rangeExprInstance.limits match {
      case Some(limit) => limit.toString
      case None        => ""
    }
    s"$startCode$limitCode$endCode"
  }
  def codeForExprReference(filename: String, parentFullname: String, referenceExprInstance: ExprReference): String = {
    val exprCode = referenceExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val code = referenceExprInstance.mut match {
      case Some(true) => s"&mut ${exprCode}"
      case _          => s"&${exprCode}"
    }
    code
  }
  def codeForExprRepeat(filename: String, parentFullname: String, repeatExprInstance: ExprRepeat): String = {
    val exprCode = repeatExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val lenCode = repeatExprInstance.len match {
      case Some(len) => codeForExpr(filename, parentFullname, len)
      case None      => Defines.Unknown
    }
    s"[$exprCode; $lenCode]"

  }
  def codeForExprReturn(filename: String, parentFullname: String, returnExprInstance: ExprReturn): String = {
    returnExprInstance.expr match {
      case Some(expr) =>
        s"return ${codeForExpr(filename, parentFullname, expr)};"
      case None => ""
    }
  }
  def codeForExprStruct(filename: String, parentFullname: String, structExprInstance: ExprStruct): String = {
    val typeFullname = structExprInstance.path match {
      case Some(path) => typeFullnameForPath(filename, parentFullname, path, structExprInstance.qself)
      case None       => Defines.Unknown
    }
    val fieldsCode =
      s"{ ${structExprInstance.fields.map(codeForFieldValue(filename, parentFullname, _)).mkString(", ")} }"

    s"$typeFullname $fieldsCode"
  }
  def codeForExprTry(filename: String, parentFullname: String, tryExprInstance: ExprTry): String = {
    "?"
  }
  def codeForExprTryBlock(filename: String, parentFullname: String, tryBlockExprInstance: ExprTryBlock): String = {
    "{}"
  }
  def codeForExprTuple(filename: String, parentFullname: String, tupleExprInstance: ExprTuple): String = {
    s"(${tupleExprInstance.elems.map(codeForExpr(filename, parentFullname, _)).mkString(", ")})"
  }
  def codeForExprUnary(filename: String, parentFullname: String, unaryExprInstance: ExprUnary): String = {
    val exprCode = unaryExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    val opCode = unaryExprInstance.op match {
      case Some(op) => op.toString
      case None     => Defines.Unknown
    }
    s"$opCode$exprCode"
  }
  def codeForExprUnsafe(filename: String, parentFullname: String, unsafeExprInstance: ExprUnsafe): String = {
    val blockCode = codeForBlock(filename, parentFullname, unsafeExprInstance.stmts)
    s"unsafe $blockCode"
  }
  def codeForExprWhile(filename: String, parentFullname: String, whileExprInstance: ExprWhile): String = {
    "while {}"
  }
  def codeForExprYield(filename: String, parentFullname: String, yieldExprInstance: ExprYield): String = {
    val exprCode = yieldExprInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }
    s"yield $exprCode"
  }
}
