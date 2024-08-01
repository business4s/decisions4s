package decisions4s

import decisions4s.exprs.{
  And,
  Between,
  Equal,
  GreaterThan,
  GreaterThanEqual,
  In,
  LessThan,
  LessThanEqual,
  Literal,
  Minus,
  Multiply,
  NotEqual,
  Or,
  Plus,
  UnaryTest,
}

trait Expr[+Out] {
  def evaluate: Out
  def renderExpression: String
}

object Expr {

  extension [O](lhs: Expr[O]) {
    def equalsTo(rhs: Expr[O]): Expr[Boolean]                 = Equal(lhs, rhs)
    def ===(rhs: Expr[O]): Expr[Boolean]                      = Equal(lhs, rhs)
    def equalsTo(rhs: O)(using LiteralShow[O]): Expr[Boolean] = Equal(lhs, Literal(rhs))
    def ===(rhs: O)(using LiteralShow[O]): Expr[Boolean]      = Equal(lhs, Literal(rhs))

    def !==(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]           = NotEqual(lhs, rhs)
    def !==(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean] = NotEqual(lhs, Literal(rhs))
    def >(rhs: Expr[O])(using Ordering[O]): Expr[Boolean]             = GreaterThan(lhs, rhs)
    def >(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[Boolean]   = GreaterThan(lhs, Literal(rhs))

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

  extension [I](lhs: Expr[Boolean]) {
    def &&(rhs: Expr[Boolean]): Expr[Boolean]        = And(lhs, rhs)
    infix def and(rhs: Expr[Boolean]): Expr[Boolean] = And(lhs, rhs)
    def ||(rhs: Expr[Boolean]): Expr[Boolean]        = Or(lhs, rhs)
    infix def or(rhs: Expr[Boolean]): Expr[Boolean]  = Or(lhs, rhs)
  }

}
