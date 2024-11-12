package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.rustsrc2cpg.ast.Block
import io.joern.rustsrc2cpg.ast.Local
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.joern.x2cpg.utils.NodeBuilders.newThisParameterNode
import io.shiftleft.codepropertygraph.generated.ControlStructureTypes
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.nodes.Block.PropertyDefaults as BlockDefaults

import scala.collection.mutable.ListBuffer

trait AstForStmt(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForBlock(filename: String, parentFullname: String, blockInstance: Block): Ast = {
    val stmtsAst = blockInstance.map(astForStmt(filename, parentFullname, _)).toList
    val code     = codeForBlock(filename, parentFullname, blockInstance)
    val wrapper  = blockNode(WrapperAst(), code, "")
    blockAst(wrapper, stmtsAst)
  }

  def astForStmt(filename: String, parentFullname: String, stmtInstance: Stmt): Ast = {
    if (stmtInstance.letStmt.isDefined) {
      return astForLocal(filename, parentFullname, stmtInstance.letStmt.get)
    } else if (stmtInstance.itemStmt.isDefined) {
      return astForItem(filename, parentFullname, stmtInstance.itemStmt.get)
    } else if (stmtInstance.exprStmt.isDefined) {
      return astForExpr(filename, parentFullname, stmtInstance.exprStmt.get._1)
    } else if (stmtInstance.macroStmt.isDefined) {
      return astForMacroStmt(filename, parentFullname, stmtInstance.macroStmt.get)
    } else {
      throw new RuntimeException(s"Unknown fnArg type: $stmtInstance")
    }
  }

  def astForMacroStmt(filename: String, parentFullname: String, macroStmtInstance: StmtMacro): Ast = {
    val annotationsAst = macroStmtInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val macroRustAst = Macro(macroStmtInstance.path, macroStmtInstance.delimiter, macroStmtInstance.tokens)
    astForMacro(filename, parentFullname, macroRustAst).withChildren(annotationsAst)
  }

  def astForLocal(filename: String, parentFullname: String, localInstance: Local): Ast = {
    val annotationsAst = localInstance.attrs match {
      case Some(attrs) => attrs.map(astForAttribute(filename, parentFullname, _)).toList
      case None        => List()
    }

    val localInitAst = localInstance.init match {
      case Some(init) => astForLocalInit(filename, parentFullname, init)
      case None       => Ast()
    }

    val name = localInstance.pat match {
      case Some(pat) => codeForPat(filename, parentFullname, pat)
      case None      => Defines.Unknown
    }
    val code = s"let $name"
    // val node = localNode(localInstance, name, code, "")
    // localNodeMap.put(name, node)

    val patAst = localInstance.pat match {
      case Some(pat) => astForPat(filename, parentFullname, pat)
      case None      => Ast()
    }

    Ast(unknownNode(localInstance, code))
      // .withChild(Ast(node))
      .withChild(patAst)
      .withChild(localInitAst)
      .withChildren(annotationsAst)
  }

  def astForLocalInit(filename: String, parentFullname: String, localInitInstance: LocalInit): Ast = {
    val exprAst = localInitInstance.expr match {
      case Some(expr) => astForExpr(filename, parentFullname, expr)
      case None       => Ast()
    }
    val divergeAst = localInitInstance.diverge match {
      case Some(diverge) => {
        val divergeExpr = astForExpr(filename, parentFullname, diverge)
        val elseNode    = controlStructureNode(ExprElse(), ControlStructureTypes.ELSE, "")
        controlStructureAst(elseNode, None, Seq(divergeExpr))
      }
      case None => Ast()
    }

    Ast(unknownNode(localInitInstance, ""))
      .withChild(exprAst)
      .withChild(divergeAst)
  }
}

trait CodeForStmt(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>

  def codeForBlock(filename: String, parentFullname: String, blockInstance: Block): String = {
    val stmtsCode = blockInstance.map(codeForStmt(filename, parentFullname, _)).mkString("\n")
    s"""
    {$stmtsCode}
    """.stripMargin
  }

  def codeForStmt(filename: String, parentFullname: String, stmtInstance: Stmt): String = {
    if (stmtInstance.letStmt.isDefined) {
      return codeForLocal(filename, parentFullname, stmtInstance.letStmt.get)
    } else if (stmtInstance.itemStmt.isDefined) {
      return "codeForItem"
    } else if (stmtInstance.exprStmt.isDefined) {
      return codeForExpr(filename, parentFullname, stmtInstance.exprStmt.get._1)
    } else if (stmtInstance.macroStmt.isDefined) {
      return codeForMacroStmt(filename, parentFullname, stmtInstance.macroStmt.get)
    } else {
      throw new RuntimeException(s"Unknown fnArg type: $stmtInstance")
    }
  }

  def codeForMacroStmt(filename: String, parentFullname: String, macroStmtInstance: StmtMacro): String = {
    codeForMacro(
      filename,
      parentFullname,
      Macro(macroStmtInstance.path, macroStmtInstance.delimiter, macroStmtInstance.tokens)
    )._2
  }

  def codeForLocal(filename: String, parentFullname: String, localInstance: Local): String = {
    val name = localInstance.pat match {
      case Some(pat) => codeForPat(filename, parentFullname, pat)
      case None      => Defines.Unknown
    }
    s"let $name"
  }

  def codeForLocalInit(filename: String, parentFullname: String, localInitInstance: LocalInit): String = {
    var code = localInitInstance.expr match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => ""
    }
    code = localInitInstance.diverge match {
      case Some(diverge) => {
        val divergeCode = codeForExpr(filename, parentFullname, diverge)
        s"""$code else {
          $divergeCode
        }""".stripMargin
      }
      case None => code
    }
    code
  }
}
