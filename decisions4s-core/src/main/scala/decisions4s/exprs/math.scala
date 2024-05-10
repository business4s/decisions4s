package decisions4s.exprs

import decisions4s.Expr

import scala.math.Ordered.orderingToOrdered

case class GreaterThan[T: Ordering](rhs: Expr[T, T]) extends Expr[T, Boolean] {
  override def evaluate(in: T): Boolean     = in > rhs.evaluate(in)
  override def renderFeelExpression: String = s"> ${rhs.renderFeelExpression}"
}

case class LessThan[T: Ordering](rhs: Expr[T, T]) extends Expr[T, Boolean] {
  override def evaluate(in: T): Boolean     = in < rhs.evaluate(in)
  override def renderFeelExpression: String = s"< ${rhs.renderFeelExpression}"
}
