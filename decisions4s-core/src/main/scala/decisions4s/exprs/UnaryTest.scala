package decisions4s.exprs

import decisions4s.Expr
import decisions4s.exprs.UnaryTest.Bool
import decisions4s.exprs.UnaryTest.Compare.Sign

import scala.language.implicitConversions
import scala.math.Ordered.orderingToOrdered

sealed trait UnaryTest[-T] extends Expr[T, Boolean] {
  def evaluate(in: T): Boolean
  def renderFeelExpression: String
}

// matches https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-unary-tests/
object UnaryTest extends LowPriorityUnaryTestConversion {

  case class EqualTo[T](expr: Expr[T, T]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean     = expr.evaluate(in) == in
    override def renderFeelExpression: String = expr.renderFeelExpression
  }

  case class OneOf[T](expr: Expr[T, Iterable[T]]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean     = expr.evaluate(in).exists(_ == in)
    override def renderFeelExpression: String = expr.renderFeelExpression
  }

  object CatchAll extends UnaryTest[Any] {
    override def evaluate(in: Any): Boolean   = true
    override def renderFeelExpression: String = "-"
  }

  case class Compare[T: Ordering](sign: Compare.Sign, rhs: Expr[T, T]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = sign match {
      case Sign.`<`  => in < rhs.evaluate(in)
      case Sign.`<=` => in <= rhs.evaluate(in)
      case Sign.`>`  => in > rhs.evaluate(in)
      case Sign.`>=` => in >= rhs.evaluate(in)
    }

    override def renderFeelExpression: String = (sign match {
      case Sign.`<`  => "<"
      case Sign.`<=` => "<="
      case Sign.`>`  => ">"
      case Sign.`>=` => ">="
    }) + s" ${rhs.renderFeelExpression}"
  }

  object Compare {
    sealed trait Sign
    object Sign {
      case object `<`  extends Sign
      case object `<=` extends Sign
      case object `>`  extends Sign
      case object `>=` extends Sign
    }
  }

  // Intervals not supported yet

  case class Or[T](parts: Seq[UnaryTest[T]]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean     = parts.exists(_.evaluate(in))
    override def renderFeelExpression: String = parts.map(_.renderFeelExpression).mkString(", ")
  }
  case class Not[I](inner: UnaryTest[I])     extends UnaryTest[I] {
    override def evaluate(in: I): Boolean     = !inner.evaluate(in)
    override def renderFeelExpression: String = s"not(${inner.renderFeelExpression})"
  }

  case class Bool[T](expr: Expr[T, Boolean]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean     = expr.evaluate(in)
    override def renderFeelExpression: String = expr.renderFeelExpression
  }

  implicit def oneOf[T](expr: Expr[T, Iterable[T]]): UnaryTest[T] = OneOf(expr)
  implicit def equalTo[T](expr: Expr[T, T]): UnaryTest[T]         = EqualTo(expr)

  extension [I](lhs: UnaryTest[I]) {
    def unary_! : UnaryTest[I]              = Not(lhs)
    def ||(rhs: UnaryTest[I]): UnaryTest[I] = Or(Seq(lhs, rhs))
  }
}

trait LowPriorityUnaryTestConversion {
  implicit def bool[T](expr: Expr[T, Boolean]): UnaryTest[T] = Bool(expr)
}
