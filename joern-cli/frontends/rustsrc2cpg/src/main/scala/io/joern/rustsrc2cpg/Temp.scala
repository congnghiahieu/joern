package io.joern.rustsrc2cpg

import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.{Encoder, Json}

import io.shiftleft.semanticcpg.language.types.expressions.generalizations.CfgNodeTraversal
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.shiftleft.codepropertygraph.generated.NodeTypes
import io.shiftleft.codepropertygraph.generated.nodes.AstNode
import io.shiftleft.codepropertygraph.generated.nodes
import io.joern.dataflowengineoss.language._
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.language.types.expressions.CallTraversal
import io.shiftleft.semanticcpg.language.types.structure.LocalTraversal
import io.shiftleft.codepropertygraph.generated.nodes.MethodParameterIn

import flatgraph.{Edge, GNode, Accessors}
import io.shiftleft.codepropertygraph.generated.nodes.AstNode

final case class GraphForFuncsFunction(
  function: String,
  file: String,
  id: String,
  AST: List[nodes.AstNode],
  CFG: List[nodes.AstNode],
  PDG: List[nodes.AstNode]
)
final case class GraphForFuncsResult(functions: List[GraphForFuncsFunction])

implicit val encodeEdge: Encoder[Edge] =
  (edge: Edge) =>
    Json.obj(
      ("id", Json.fromString(edge.toString)),
      ("in", Json.fromString(edge.dst.toString)),
      ("out", Json.fromString(edge.src.toString))
    )

implicit val encodeNode: Encoder[nodes.AstNode] =
  (node: nodes.AstNode) =>
    Json.obj(
      ("id", Json.fromString(node.toString)),
      ("edges", Json.fromValues((node.inE.l ++ node.outE.l).map(_.asJson))),
      (
        "properties",
        Json.fromValues(node.properties.map { case (key, value) =>
          Json.obj(("key", Json.fromString(key)), ("value", Json.fromString(value.toString)))
        })
      )
    )

implicit val encodeFuncFunction: Encoder[GraphForFuncsFunction] = deriveEncoder
implicit val encodeFuncResult: Encoder[GraphForFuncsResult]     = deriveEncoder
