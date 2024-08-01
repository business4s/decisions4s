package decisions4s.exprs

import decisions4s.Expr

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-numeric-expressions/

class Plus[O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].plus(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} + ${rhs.renderExpression}"
}

class Minus[I, O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].minus(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} - ${rhs.renderExpression}"
}

class Multiply[O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].times(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} * ${rhs.renderExpression}"
}

// no division and power for now because it can't be implemented generically
