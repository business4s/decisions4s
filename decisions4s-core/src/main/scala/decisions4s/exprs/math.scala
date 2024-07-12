package decisions4s.exprs

import decisions4s.Expr

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-numeric-expressions/


class Plus[I, O](lhs: Expr[I, O], rhs: Expr[I, O])(using Numeric[O]) extends Expr[I, O] {
  override def evaluate(in: I): O = Numeric[O].plus(lhs.evaluate(in), rhs.evaluate(in))
  override def renderExpression: String = s"${lhs.renderExpression} + ${rhs.renderExpression}"
}

class Minus[I, O](lhs: Expr[I, O], rhs: Expr[I, O])(using Numeric[O]) extends Expr[I, O] {
  override def evaluate(in: I): O = Numeric[O].minus(lhs.evaluate(in), rhs.evaluate(in))
  override def renderExpression: String = s"${lhs.renderExpression} - ${rhs.renderExpression}"
}

class Multiply[I, O](lhs: Expr[I, O], rhs: Expr[I, O])(using Numeric[O]) extends Expr[I, O] {
  override def evaluate(in: I): O = Numeric[O].times(lhs.evaluate(in), rhs.evaluate(in))
  override def renderExpression: String = s"${lhs.renderExpression} * ${rhs.renderExpression}"
}

// no division and power for now because it can't be implemented generically