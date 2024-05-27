package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

case class Literal[T](v: T)(using show: LiteralShow[T]) extends Expr[Any, T] {
  override def evaluate(in: Any): T         = v
  override def renderFeelExpression: String = show.show(v)
}

case class Input[T]() extends Expr[T, T] {
  override def evaluate(in: T): T           = in
  override def renderFeelExpression: String = "?"
}

case class Comment[I, O](inner: Expr[I, O], comment: String) extends Expr[I, O] {
  override def evaluate(in: I): O = inner.evaluate(in)

  override def renderFeelExpression: String = {
    val lines      = comment.linesIterator.toList
    val commentStr: String =
      if (lines.size == 1) comment.prependedAll("// ")
      else (List("/*") ++ lines.map(_.prependedAll(" * ")) ++ List(" */")).mkString("\n")
    s"""$commentStr
       |${inner.renderFeelExpression}""".stripMargin
  }
}
