package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

case class Literal[T](v: T)(using show: LiteralShow[T]) extends Expr[Any, T] {
  override def evaluate(in: Any): T         = v
  override def renderFeelExpression: String = show.show(v)
}
