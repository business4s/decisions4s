package decisions4s.exprs

import decisions4s.{Expr, LiteralShow}

import scala.util.chaining.scalaUtilChainingOps

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

// should be used only for rendering
case class VariableStub[T](name: String) extends Expr[T] {
  override def evaluate: T              = ???
  override def renderExpression: String = name
}

class Projection[O1, +O2](val base: Expr[O1], val get: O1 => Expr[O2], val label: String) extends Expr[O2] {
  override def evaluate: O2             = base.evaluate.pipe(get).evaluate
  override def renderExpression: String = s"${base.renderExpression}.$label"
}

object Projection {
  def apply[O1, O2](base: Expr[O1], get: O1 => Expr[O2], label: String): Projection[O1, O2] = new Projection(base, get, label)
  def unapply[O1, O2](x: Projection[O1, O2]): Option[(Expr[O1], O1 => Expr[O2], String)] = Some((x.base, x.get, x.label))
}

class IsEmpty[T](val base: Expr[Option[T]]) extends Expr[Boolean] {
  override def evaluate: Boolean        = base.evaluate.isEmpty
  override def renderExpression: String = s"isEmpty(${base.renderExpression})"
}

object IsEmpty {
  def apply[T](base: Expr[Option[T]]): IsEmpty[T] = new IsEmpty(base)
  def unapply[T](x: IsEmpty[T]): Option[Expr[Option[T]]] = Some(x.base)
}
