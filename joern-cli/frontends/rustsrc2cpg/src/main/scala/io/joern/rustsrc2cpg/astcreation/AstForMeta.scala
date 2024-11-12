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

type CodeForReturnType = (String, String, String)

trait AstForMeta(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForMeta(filename: String, parentFullname: String, metaInstance: Meta): Ast = {
    if (metaInstance.path.isDefined) {
      astForPath(filename, parentFullname, metaInstance.path.get)
    } else if (metaInstance.list.isDefined) {
      astForMetaList(filename, parentFullname, metaInstance.list.get)
    } else if (metaInstance.nameValue.isDefined) {
      astForMetaNameValue(filename, parentFullname, metaInstance.nameValue.get)
    } else {
      throw new IllegalArgumentException("Unsupported meta type")
    }
  }
  def astForPath(filename: String, parentFullname: String, pathInstance: Path, qself: Option[QSelf] = None): Ast = {
    val (fullname, _, code) = codeForPath(filename, parentFullname, pathInstance, qself)
    getCurrentPathCpgNodeType match {
      case PathCPGNodeType.IDENTIFIER_NODE => {
        val node = identifierNode(pathInstance, fullname, fullname, "")
        val nodeAst = scope.lookupVariable(fullname) match {
          case Some((newNode, _)) => {
            newNode match {
              case localNode: NewLocal => Ast(node).withRefEdge(node, localNode)
              case _                   => Ast(node)
            }
          }
          case None => Ast(node)
        }
        nodeAst
      }
      case PathCPGNodeType.TYPEREF_NODE => {
        val node = typeRefNode(pathInstance, fullname, fullname)
        val nodeAst = scope.lookupVariable(fullname) match {
          case Some((newNode, _)) => {
            newNode match {
              case typeNode: NewType         => Ast(node).withRefEdge(node, typeNode)
              case typeDeclNode: NewTypeDecl => Ast(node).withRefEdge(node, typeDeclNode)
              case _                         => Ast(node)
            }
          }
          case None => Ast(node)
        }
        nodeAst
      }
      case PathCPGNodeType.METHODREF_NODE => {
        val node = methodRefNode(pathInstance, fullname, fullname, "")
        val nodeAst = scope.lookupVariable(fullname) match {
          case Some((newNode, _)) => {
            newNode match {
              case methodNode: NewMethod => Ast(node).withRefEdge(node, methodNode)
              case _                     => Ast(node)
            }
          }
          case None => Ast(node)
        }
        nodeAst
      }
    }
  }

  def astForMetaList(filename: String, parentFullname: String, metaListInstance: MetaList): Ast = {
    val (typeFullname, inputToken, code) = codeForMetaList(filename, parentFullname, metaListInstance)

    setCurrentPathCpgNodeType(PathCPGNodeType.METHODREF_NODE)
    val pathAst = metaListInstance.path match {
      case Some(path) => astForPath(filename, parentFullname, path)
      case None       => Ast()
    }

    annotationAssignmentAst(inputToken, code, pathAst)
  }

  def astForMetaNameValue(filename: String, parentFullname: String, metaNameValueInstance: MetaNameValue): Ast = {
    val (typeFullname, exprValue, code) = codeForMetaNameValue(filename, parentFullname, metaNameValueInstance)

    setCurrentPathCpgNodeType(PathCPGNodeType.METHODREF_NODE)
    val pathAst = metaNameValueInstance.path match {
      case Some(path) => astForPath(filename, parentFullname, path)
      case None       => Ast()
    }

    annotationAssignmentAst(exprValue, code, pathAst)
  }
}

trait CodeForMeta(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def codeForMeta(filename: String, parentFullname: String, metaInstance: Meta): CodeForReturnType = {
    if (metaInstance.path.isDefined) {
      codeForPath(filename, parentFullname, metaInstance.path.get)
    } else if (metaInstance.list.isDefined) {
      codeForMetaList(filename, parentFullname, metaInstance.list.get)
    } else if (metaInstance.nameValue.isDefined) {
      codeForMetaNameValue(filename, parentFullname, metaInstance.nameValue.get)
    } else {
      throw new IllegalArgumentException("Unsupported meta type")
    }
  }

  def codeForPath(
    filename: String,
    parentFullname: String,
    pathInstance: Path,
    qself: Option[QSelf] = None
  ): CodeForReturnType = {
    val typeFullname = typeFullnameForPath(filename, parentFullname, pathInstance, qself)
    val input        = typeFullname // temporary
    val code         = typeFullname // temporary
    (typeFullname, input, code)
  }

  def codeForMetaList(filename: String, parentFullname: String, metaListInstance: MetaList): CodeForReturnType = {
    val typeFullname = metaListInstance.path match {
      case Some(path) => typeFullnameForPath(filename, parentFullname, path)
      case None       => Defines.Unknown
    }
    val inputToken = metaListInstance.tokens match {
      case Some(tokens) => codeForTokenStream(filename, parentFullname, tokens)
      case None         => Defines.Unknown
    }
    val code = metaListInstance.delimiter match {
      case Some(delimiter) => {
        delimiter match {
          case MacroDelimiter.Paren   => s"${typeFullname}(${inputToken})"
          case MacroDelimiter.Brace   => s"${typeFullname}{${inputToken}}"
          case MacroDelimiter.Bracket => s"${typeFullname}[${inputToken}]"
        }
      }
      case None => s"${typeFullname}${inputToken}"
    }

    (typeFullname, inputToken, code)
  }

  def codeForMetaNameValue(
    filename: String,
    parentFullname: String,
    metaNameValueInstance: MetaNameValue
  ): CodeForReturnType = {
    val typeFullname = metaNameValueInstance.path match {
      case Some(path) => typeFullnameForPath(filename, parentFullname, path)
      case None       => Defines.Unknown
    }

    val exprValue = metaNameValueInstance.value match {
      case Some(expr) => codeForExpr(filename, parentFullname, expr)
      case None       => Defines.Unknown
    }

    val code = metaNameValueInstance.value match {
      case Some(expr) => s"${typeFullname} = ${exprValue}"
      case None       => Defines.Unknown
    }

    (typeFullname, exprValue, code)
  }

  def typeFullnameForPath(
    filename: String,
    parentFullname: String,
    pathInstance: Path,
    qself: Option[QSelf] = None
  ): String = {
    qself match {
      case Some(qself) => {
        val typeFullnameOfQself = qself.ty match {
          case Some(ty) => typeFullnameForType(filename, parentFullname, ty)
          case None     => Defines.Unknown
        }
        val segments = typeFullnameForListPathSegments(filename, parentFullname, pathInstance.segments)

        qself.position match {
          case 0 => {
            pathInstance.leading_colon match {
              case Some(true) => s"<$typeFullnameOfQself>::${segments.head}"
              case _          => s"<$typeFullnameOfQself>${segments.head}"
            }
          }
          case _ => {
            val targetedSegment           = segments.remove(qself.position)
            val otherSegmentsAsFullString = segments.mkString("::")

            qself.as_token match {
              case Some(true) => {
                s"<$typeFullnameOfQself as ${otherSegmentsAsFullString}>::$targetedSegment"
              }
              case _ => {
                s"${typeFullnameOfQself} ${otherSegmentsAsFullString}::$targetedSegment"
              }
            }
          }
        }
      }
      case None => {
        val identFullString =
          typeFullnameForListPathSegments(filename, parentFullname, pathInstance.segments).mkString("::")
        val typeFullname = pathInstance.leading_colon match {
          case Some(_) => s"::${identFullString}"
          case None    => identFullString
        }
        typeFullname
      }
    }
  }

  def typeFullnameForListPathSegments(
    filename: String,
    parentFullname: String,
    listPathSegments: ListBuffer[PathSegment]
  ): ListBuffer[String] = {
    val segmentIdents = listPathSegments.map(typeFullnameForPathSegment(filename, parentFullname, _))
    segmentIdents
  }

  def typeFullnameForPathSegment(filename: String, parentFullname: String, pathSegmentInstance: PathSegment): String = {
    pathSegmentInstance.arguments match {
      case Some(arguments) => {
        val args = codeForPathArguments(filename, parentFullname, arguments)
        s"${pathSegmentInstance.ident}${args}"
      }
      case None => pathSegmentInstance.ident
    }
  }
}
