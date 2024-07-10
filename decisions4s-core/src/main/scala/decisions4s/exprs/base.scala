package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

case class Literal[T](v: T)(using show: LiteralShow[T]) extends Expr[Any, T] {
  override def evaluate(in: Any): T     = v
  override def renderExpression: String = show.show(v)
}

case class Input[T]() extends Expr[T, T] {
  override def evaluate(in: T): T       = in
  override def renderExpression: String = "?"
}

case class Comment[I, O](inner: Expr[I, O], comment: String) extends Expr[I, O] {
  override def evaluate(in: I): O = inner.evaluate(in)

  override def renderExpression: String = {
    val lines              = comment.linesIterator.toList
    val commentStr: String =
      if (lines.size == 1) comment.prependedAll("// ")
      else (List("/*") ++ lines.map(_.prependedAll(" * ")) ++ List(" */")).mkString("\n")
    s"""$commentStr
       |${inner.renderExpression}""".stripMargin
  }
}

case class Variable[T](name: String, value: T) extends Expr[Any, T] {
  override def evaluate(in: Any): T = value
  override def renderExpression: String = name
}

// used only for rendering
case class VariableStub[T](name: String) extends Expr[Any, T] {
  override def evaluate(in: Any): T = ???
  override def renderExpression: String = name
}

