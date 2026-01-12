package decisions4s

import decisions4s.HKD.FieldUtils
import decisions4s.exprs.{
  And,
  Between,
  Equal,
  GreaterThan,
  GreaterThanEqual,
  In,
  IsEmpty,
  LessThan,
  LessThanEqual,
  Literal,
  Minus,
  Multiply,
  NotEqual,
  Or,
  Plus,
  Projection,
  UnaryTest,
}
import scala.quoted.*

trait Expr[+Out] {
  def evaluate: Out
  def renderExpression: String
}

object Expr {
  inline def quoted[T](inline expr: T): Expr[T] = ${ quotedImpl('expr) }

  private def quotedImpl[T: Type](expr: scala.quoted.Expr[T])(using Quotes): scala.quoted.Expr[Expr[T]] = {
    import quotes.reflect.*
    val pos = expr.asTerm.pos
    val code = pos.sourceCode.getOrElse("???")
    '{
      new Expr[T] {
        def evaluate: T = $expr
        def renderExpression: String = ${scala.quoted.Expr(code)}
      }
    }
  }

  extension [O](lhs: Expr[O]) {
    def equalsTo(rhs: Expr[O]): Expr[Boolean]                 = Equal(lhs, rhs)
    def ===(rhs: Expr[O]): Expr[Boolean]                      = Equal(lhs, rhs)
    def equalsTo(rhs: O)(using LiteralShow[O]): Expr[Boolean] = Equal(lhs, Literal(rhs))
    def ===(rhs: O)(using LiteralShow[O]): Expr[Boolean]      = Equal(lhs, Literal(rhs))

    def !==(rhs: Expr[O]): Expr[Boolean]                            = NotEqual(lhs, rhs)
    def !==(rhs: O)(using LiteralShow[O]): Expr[Boolean]            = NotEqual(lhs, Literal(rhs))
    def >(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]           = GreaterThan(lhs, rhs)
    def >(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean] = GreaterThan(lhs, Literal(rhs))

    def >=(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]           = GreaterThanEqual(lhs, rhs)
    def >=(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean] = GreaterThanEqual(lhs, Literal(rhs))
    def <(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]            = LessThan(lhs, rhs)
    def <(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean]  = LessThan(lhs, Literal(rhs))
    def <=(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]           = LessThanEqual(lhs, rhs)
    def <=(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean] = LessThanEqual(lhs, Literal(rhs))

    def +(rhs: Expr[O])(using Numeric[O]): Expr[O]           = Plus(lhs, rhs)
    def +(rhs: O)(using Numeric[O], LiteralShow[O]): Expr[O] = Plus(lhs, Literal(rhs))
    def -(rhs: Expr[O])(using Numeric[O]): Expr[O]           = Minus(lhs, rhs)
    def -(rhs: O)(using Numeric[O], LiteralShow[O]): Expr[O] = Minus(lhs, Literal(rhs))
    def *(rhs: Expr[O])(using Numeric[O]): Expr[O]           = Multiply(lhs, rhs)
    def *(rhs: O)(using Numeric[O], LiteralShow[O]): Expr[O] = Multiply(lhs, Literal(rhs))

    def between(lowerBound: Expr[O], upperBound: Expr[O])(using Ordering[O]): Expr[Boolean] = Between(lhs, lowerBound, upperBound)

    infix def in(rhs: UnaryTest[O]): Expr[Boolean] = In(lhs, rhs)
  }

  extension [T](lhs: Expr[Option[T]]) {
    def isEmpty = IsEmpty(lhs)
  }

  extension (lhs: Expr[Boolean]) {
    def &&(rhs: Expr[Boolean]): Expr[Boolean]        = And(lhs, rhs)
    infix def and(rhs: Expr[Boolean]): Expr[Boolean] = And(lhs, rhs)
    def ||(rhs: Expr[Boolean]): Expr[Boolean]        = Or(lhs, rhs)
    infix def or(rhs: Expr[Boolean]): Expr[Boolean]  = Or(lhs, rhs)
  }

  extension [Data[_[_]]](in: Expr[Data[Expr]]) {
    def projection(using hkd: HKD[Data]): Data[Expr] = {
      hkd.construct([t] => (fu: FieldUtils[Data, t]) => Projection[Data[Expr], t](in, fu.extract, fu.name))
    }
    def prj(using hkd: HKD[Data]): Data[Expr]        = projection
  }

}
