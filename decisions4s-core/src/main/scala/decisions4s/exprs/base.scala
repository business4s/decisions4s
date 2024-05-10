package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

class Literal[T](v: T)(using show: LiteralShow[T]) extends Expr[T] {
  override def evaluate: T      = v
  override def describe: String = show.show(v)
}

class Variable[T](name: String) extends Expr[T] {
  override def evaluate: T      = ???
  override def describe: String = name
}
