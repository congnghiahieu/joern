package io.joern.rustsrc2cpg.ast

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import io.joern.rustsrc2cpg.ast.*
import io.shiftleft.codepropertygraph.generated.{Operators}

enum BinOp(val value: String) extends RustAst {
  case Add          extends BinOp("+")
  case Sub          extends BinOp("-")
  case Mul          extends BinOp("*")
  case Div          extends BinOp("/")
  case Rem          extends BinOp("%")
  case And          extends BinOp("&&")
  case Or           extends BinOp("||")
  case BitXor       extends BinOp("^")
  case BitAnd       extends BinOp("&")
  case BitOr        extends BinOp("|")
  case Shl          extends BinOp("<<")
  case Shr          extends BinOp(">>")
  case Eq           extends BinOp("==")
  case Lt           extends BinOp("<")
  case Le           extends BinOp("<=")
  case Ne           extends BinOp("!=")
  case Ge           extends BinOp(">=")
  case Gt           extends BinOp(">")
  case AddAssign    extends BinOp("+=")
  case SubAssign    extends BinOp("-=")
  case MulAssign    extends BinOp("*=")
  case DivAssign    extends BinOp("/=")
  case RemAssign    extends BinOp("%=")
  case BitXorAssign extends BinOp("^=")
  case BitAndAssign extends BinOp("&=")
  case BitOrAssign  extends BinOp("|=")
  case ShlAssign    extends BinOp("<<=")
  case ShrAssign    extends BinOp(">>=")

  @JsonValue
  override def toString: String = value
}

object BinOp {
  @JsonCreator
  def fromString(value: String): BinOp = value match {
    case "+"   => BinOp.Add
    case "-"   => BinOp.Sub
    case "*"   => BinOp.Mul
    case "/"   => BinOp.Div
    case "%"   => BinOp.Rem
    case "&&"  => BinOp.And
    case "||"  => BinOp.Or
    case "^"   => BinOp.BitXor
    case "&"   => BinOp.BitAnd
    case "|"   => BinOp.BitOr
    case "<<"  => BinOp.Shl
    case ">>"  => BinOp.Shr
    case "=="  => BinOp.Eq
    case "<"   => BinOp.Lt
    case "<="  => BinOp.Le
    case "!="  => BinOp.Ne
    case ">="  => BinOp.Ge
    case ">"   => BinOp.Gt
    case "+="  => BinOp.AddAssign
    case "-="  => BinOp.SubAssign
    case "*="  => BinOp.MulAssign
    case "/="  => BinOp.DivAssign
    case "%="  => BinOp.RemAssign
    case "^="  => BinOp.BitXorAssign
    case "&="  => BinOp.BitAndAssign
    case "|="  => BinOp.BitOrAssign
    case "<<=" => BinOp.ShlAssign
    case ">>=" => BinOp.ShrAssign
    case _     => throw new IllegalArgumentException(s"Unknown value: $value")
  }

  def binOpToOperator(binOp: BinOp) = {
    binOp match {
      case BinOp.Add          => Operators.addition
      case BinOp.Sub          => Operators.subtraction
      case BinOp.Mul          => Operators.multiplication
      case BinOp.Div          => Operators.division
      case BinOp.Rem          => Operators.modulo
      case BinOp.And          => Operators.and
      case BinOp.Or           => Operators.or
      case BinOp.BitXor       => Operators.xor
      case BinOp.BitAnd       => Operators.and
      case BinOp.BitOr        => Operators.or
      case BinOp.Shl          => Operators.shiftLeft
      case BinOp.Shr          => Operators.logicalShiftRight
      case BinOp.Eq           => Operators.equals
      case BinOp.Lt           => Operators.lessThan
      case BinOp.Le           => Operators.lessEqualsThan
      case BinOp.Ne           => Operators.notEquals
      case BinOp.Ge           => Operators.greaterEqualsThan
      case BinOp.Gt           => Operators.greaterThan
      case BinOp.AddAssign    => Operators.assignmentPlus
      case BinOp.SubAssign    => Operators.assignmentMinus
      case BinOp.MulAssign    => Operators.assignmentMultiplication
      case BinOp.DivAssign    => Operators.assignmentDivision
      case BinOp.RemAssign    => Operators.assignmentModulo
      case BinOp.BitXorAssign => Operators.assignmentAnd
      case BinOp.BitAndAssign => Operators.assignmentAnd
      case BinOp.BitOrAssign  => Operators.assignmentOr
      case BinOp.ShlAssign    => Operators.assignmentShiftLeft
      case BinOp.ShrAssign    => Operators.assignmentLogicalShiftRight
    }
  }

}

enum UnOp(val value: String) extends RustAst {
  case Deref extends UnOp("*")
  case Not   extends UnOp("!")
  case Neg   extends UnOp("-")

  @JsonValue
  override def toString: String = value
}

object UnOp {
  @JsonCreator
  def fromString(value: String): UnOp = value match {
    case "*" => UnOp.Deref
    case "!" => UnOp.Not
    case "-" => UnOp.Neg
    case _   => throw new IllegalArgumentException(s"Unknown value: $value")
  }

  def unOpToOperator(unOp: UnOp) = {
    unOp match {
      case UnOp.Deref => Operators.indirection
      case UnOp.Not   => Operators.logicalNot
      case UnOp.Neg   => Operators.minus
    }
  }
}
