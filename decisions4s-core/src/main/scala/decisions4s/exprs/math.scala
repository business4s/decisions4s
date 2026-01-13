package decisions4s.exprs

import decisions4s.Expr

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-numeric-expressions/

class Plus[O](val lhs: Expr[O], val rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].plus(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} + ${rhs.renderExpression}"
}

object Plus {
  def apply[O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]): Plus[O] = new Plus(lhs, rhs)
  def unapply[O](x: Plus[O]): Option[(Expr[O], Expr[O])]              = Some((x.lhs, x.rhs))
}

class Minus[I, O](val lhs: Expr[O], val rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].minus(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} - ${rhs.renderExpression}"
}

object Minus {
  def apply[I, O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]): Minus[I, O] = new Minus(lhs, rhs)
  def unapply[I, O](x: Minus[I, O]): Option[(Expr[O], Expr[O])]              = Some((x.lhs, x.rhs))
}

class Multiply[O](val lhs: Expr[O], val rhs: Expr[O])(using Numeric[O]) extends Expr[O] {
  override def evaluate: O              = Numeric[O].times(lhs.evaluate, rhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} * ${rhs.renderExpression}"
}

object Multiply {
  def apply[O](lhs: Expr[O], rhs: Expr[O])(using Numeric[O]): Multiply[O] = new Multiply(lhs, rhs)
  def unapply[O](x: Multiply[O]): Option[(Expr[O], Expr[O])]              = Some((x.lhs, x.rhs))
}

// no division and power for now because it can't be implemented generically
