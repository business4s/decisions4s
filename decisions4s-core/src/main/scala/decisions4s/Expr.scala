package decisions4s

import decisions4s.exprs.{And, Between, Equal, GreaterThan, GreaterThanEqual, In, LessThan, LessThanEqual, Literal, NotEqual, Or, UnaryTest}

trait Expr[-In, +Out] {
  def evaluate(in: In): Out
  def renderExpression: String
}

object Expr {

  extension [I, O](lhs: Expr[I, O]) {
    def equalsTo(rhs: Expr[I, O]): Expr[I, Boolean]              = Equal(lhs, rhs)
    def ===(rhs: Expr[I, O]): Expr[I, Boolean]                   = Equal(lhs, rhs)
    def equalsTo(rhs: O)(using LiteralShow[O]): Expr[I, Boolean] = Equal(lhs, Literal(rhs))
    def ===(rhs: O)(using LiteralShow[O]): Expr[I, Boolean]      = Equal(lhs, Literal(rhs))

    def !==(rhs: Expr[I, O])(using Ordering[O]): Expr[I, Boolean]      = NotEqual(lhs, rhs)
    def >(rhs: Expr[I, O])(using Ordering[O]): Expr[I, Boolean]        = GreaterThan(lhs, rhs)
    def >(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[I, Boolean] = GreaterThan(lhs, Literal(rhs))

    def >=(rhs: Expr[I, O])(using Ordering[O]): Expr[I, Boolean]        = GreaterThanEqual(lhs, rhs)
    def >=(rhs: O)(using Ordering[O], LiteralShow[O]): Expr[I, Boolean] = GreaterThanEqual(lhs, Literal(rhs))
    def <(rhs: Expr[I, O])(using Ordering[O]): Expr[I, Boolean]         = LessThan(lhs, rhs)
    def <=(rhs: Expr[I, O])(using Ordering[O]): Expr[I, Boolean]        = LessThanEqual(lhs, rhs)

    def between(lowerBound: Expr[I, O], upperBound: Expr[I, O])(using Ordering[O]): Expr[I, Boolean] = Between(lhs, lowerBound, upperBound)

    infix def in(rhs: UnaryTest[O]): Expr[I, Boolean] = In(lhs, rhs)
  }

  extension [I](lhs: Expr[I, Boolean]) {
    def &&(rhs: Expr[I, Boolean]): Expr[I, Boolean]  = And(lhs, rhs)
    infix def and(rhs: Expr[I, Boolean]): Expr[I, Boolean] = And(lhs, rhs)
    def ||(rhs: Expr[I, Boolean]): Expr[I, Boolean]  = Or(lhs, rhs)
    infix def or(rhs: Expr[I, Boolean]): Expr[I, Boolean]  = Or(lhs, rhs)
  }

}
