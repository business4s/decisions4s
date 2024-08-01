package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

case class Literal[T](v: T)(using show: LiteralShow[T]) extends Expr[T] {
  override def evaluate: T              = v
  override def renderExpression: String = show.show(v)
}

case class Comment[O](inner: Expr[O], comment: String) extends Expr[O] {
  override def evaluate: O = inner.evaluate

  override def renderExpression: String = {
    val lines              = comment.linesIterator.toList
    val commentStr: String =
      if (lines.size == 1) comment.prependedAll("// ")
      else (List("/*") ++ lines.map(_.prependedAll(" * ")) ++ List(" */")).mkString("\n")
    s"""$commentStr
       |${inner.renderExpression}""".stripMargin
  }
}

case class Variable[T](name: String, value: T) extends Expr[T] {
  override def evaluate: T              = value
  override def renderExpression: String = name
}

// used only for rendering
case class VariableStub[T](name: String) extends Expr[T] {
  override def evaluate: T              = ???
  override def renderExpression: String = name
}
