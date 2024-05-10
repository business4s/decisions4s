package decisions4s.exprs

import decisions4s.Expr

case class Equal[T](b: Expr[T, T]) extends Expr[T, Boolean] {
  override def evaluate(in: T): Boolean     = in == b.evaluate(in)
  override def renderFeelExpression: String = b.renderFeelExpression
}


case class Or[T](parts: List[Expr[T, Boolean]]) extends Expr[T, Boolean] {
  override def evaluate(in: T): Boolean     = parts.exists(_.evaluate(in))
  override def renderFeelExpression: String = parts.map(_.renderFeelExpression).mkString(", ")
}

object True extends Expr[Any, Boolean] {
  override def evaluate(in: Any): Boolean   = true
  override def renderFeelExpression: String = "-"
}
