package decisions4s.exprs

import decisions4s.Expr

class Literal[T](v: T) extends Expr[T] {
  override def evaluate: T      = v
  override def describe: String = v.toString // TODO, more control?
}