package io.joern.rustsrc2cpg.astcreation

import io.joern.rustsrc2cpg.ast.*
import io.joern.x2cpg.Ast
import io.joern.x2cpg.AstCreatorBase
import io.joern.x2cpg.AstNodeBuilder
import io.joern.x2cpg.Defines
import io.joern.x2cpg.ValidationMode
import io.joern.x2cpg.utils.NodeBuilders.newModifierNode
import io.joern.x2cpg.utils.NodeBuilders.newThisParameterNode
import io.shiftleft.codepropertygraph.generated.EvaluationStrategies
import io.shiftleft.codepropertygraph.generated.ModifierTypes
import io.shiftleft.codepropertygraph.generated.nodes.{
  Lifetime as LifetimeCpg,
  NewTypeParameter,
  LifetimeParameter,
  NewLifetimeParameter
}

import scala.collection.mutable.ListBuffer
import io.shiftleft.codepropertygraph.generated.nodes.NewLifetime
import io.shiftleft.codepropertygraph.generated.nodes.NewLifetimeArgument

trait AstForWherePredicate(implicit schemaValidationMode: ValidationMode) { this: AstCreator =>
  def astForWherePredicate(filename: String, parentFullname: String, wherePredicateInstance: WherePredicate): Ast = {
    if (wherePredicateInstance.lifetimeWherePredicate.isDefined) {
      astForLifetimeWherePredicate(filename, parentFullname, wherePredicateInstance.lifetimeWherePredicate.get)
    } else if (wherePredicateInstance.typeWherePredicate.isDefined) {
      astForTypeWherePredicate(filename, parentFullname, wherePredicateInstance.typeWherePredicate.get)
    } else {
      throw new RuntimeException(s"Unknown WherePredicate type: $wherePredicateInstance")
    }
  }

  def astForLifetimeWherePredicate(
    filename: String,
    parentFullname: String,
    lifetimeWherePredicateInstance: PredicateLifetime
  ): Ast = {
    val lifetimeCode = codeForLifetime(filename, parentFullname, lifetimeWherePredicateInstance.lifetime)
    val boundsCode =
      lifetimeWherePredicateInstance.bounds.map(codeForLifetime(filename, parentFullname, _)).mkString(" + ")

    val lifetimePredicateAst = astForLifetimeAsParam(filename, parentFullname, lifetimeWherePredicateInstance.lifetime)
    val boundsAst = lifetimeWherePredicateInstance.bounds.nonEmpty match {
      case true =>
        val bounds  = lifetimeWherePredicateInstance.bounds.map(astForLifetime(filename, parentFullname, _)).toList
        val wrapper = Ast(unknownNode(BoundAst(), boundsCode))
        wrapper.withChildren(bounds)
      case false => Ast()
    }

    val code = lifetimeWherePredicateInstance.bounds.nonEmpty match {
      case true  => s"$lifetimeCode: $boundsCode"
      case false => lifetimeCode
    }

    Ast(unknownNode(lifetimeWherePredicateInstance, code))
      .withChild(lifetimePredicateAst)
      .withChild(boundsAst)
  }

  def astForTypeWherePredicate(
    filename: String,
    parentFullname: String,
    typeWherePredicateInstance: PredicateType
  ): Ast = {
    val parameterName = typeWherePredicateInstance.bounded_ty match {
      case Some(bounded_ty) => typeFullnameForType(filename, parentFullname, bounded_ty)
      case None             => Defines.Unknown
    }
    val totalBoundsCode =
      (typeWherePredicateInstance.bounds.map(codeForTypeParamBound(filename, parentFullname, _))
        ++
          typeWherePredicateInstance.lifetimes
            .getOrElse(List())
            .map(codeForGenericParam(filename, parentFullname, _))).mkString(" + ")

    val lifetimesBoundsAst = typeWherePredicateInstance.lifetimes match {
      case Some(lifetimes) => lifetimes.map(astForGenericParam(filename, parentFullname, _)).toList
      case None            => List()
    }
    val boundsAst = typeWherePredicateInstance.bounds.map(astForTypeParamBound(filename, parentFullname, _)).toList
    val totalsBoundsAst = lifetimesBoundsAst ++ boundsAst
    val boundWrapper = totalsBoundsAst.nonEmpty match {
      case true =>
        val wrapper = Ast(unknownNode(BoundAst(), totalBoundsCode))
        wrapper.withChildren(totalsBoundsAst)
      case false => Ast()
    }

    val code = totalBoundsCode.nonEmpty match {
      case true =>
        s"$parameterName: $totalBoundsCode"
      case false =>
        parameterName
    }

    val node = NewTypeParameter()
      .name(parameterName)
      .code(code)

    Ast(unknownNode(typeWherePredicateInstance, code))
      .withChild(Ast(node))
      .withChild(boundWrapper)
  }

  def astForLifetimeAsArgument(filename: String, parentFullname: String, lifetimeInstance: Lifetime): Ast = {
    val code = codeForLifetime(filename, parentFullname, lifetimeInstance)
    val node = NewLifetimeArgument()
      .name(code)
      .code(code)

    Ast(node)
  }

  def astForLifetimeAsParam(filename: String, parentFullname: String, lifetimeInstance: Lifetime): Ast = {
    val code = codeForLifetime(filename, parentFullname, lifetimeInstance)
    val node = NewLifetimeParameter()
      .name(code)
      .code(code)

    Ast(node)
  }

  def astForLifetime(filename: String, parentFullname: String, lifetimeInstance: Lifetime): Ast = {
    val code = codeForLifetime(filename, parentFullname, lifetimeInstance)
    val node = NewLifetime()
      .name(code)

    Ast(node)
  }

  def codeForLifetime(filename: String, parentFullname: String, lifetimeInstance: Lifetime): String = {
    s"'${lifetimeInstance}"
  }
}
