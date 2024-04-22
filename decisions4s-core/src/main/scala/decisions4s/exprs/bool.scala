package decisions4s.exprs


import decisions4s.Expr


case class Equal[T](a: Expr[T], b: Expr[T]) extends Expr[Boolean] {
  override def evaluate: Boolean = a.evaluate == b.evaluate
  override def describe: String  = s"${a.describe} == ${b.describe}"
}


case class And(left: Expr[Boolean], right: Expr[Boolean]) extends Expr[Boolean] {
  override def evaluate: Boolean = left.evaluate && right.evaluate
  override def describe: String  = s"${left.describe} && ${right.describe}"
}

object True extends Expr[Boolean] {
  override def evaluate: Boolean = true
  override def describe: String  = "*"
}
