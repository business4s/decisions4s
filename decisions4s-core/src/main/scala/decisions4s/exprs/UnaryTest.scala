package decisions4s.exprs

import decisions4s.Expr
import decisions4s.exprs.UnaryTest.Bool
import decisions4s.exprs.UnaryTest.Compare.Sign

import scala.language.implicitConversions
import scala.math.Ordered.orderingToOrdered

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-unary-tests/
trait UnaryTest[-T] {
  def evaluate(in: T): Boolean
  def renderExpression: String
}

object UnaryTest extends LowPriorityUnaryTestConversion {

  case class WithValue[T](f: Expr[T] => Expr[Boolean]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = f(Variable("?", in)).evaluate
    override def renderExpression: String = f(VariableStub("?")).renderExpression
  }

  case class EqualTo[T](expr: Expr[T]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = in == expr.evaluate
    override def renderExpression: String = expr.renderExpression
  }

  case class OneOf[T](expr: Expr[Iterable[T]]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = expr.evaluate.exists(_ == in)
    override def renderExpression: String = expr.renderExpression
  }

  object CatchAll extends UnaryTest[Any] {
    override def evaluate(in: Any): Boolean = true
    override def renderExpression: String   = "-"
  }

  case class Compare[T: Ordering](sign: Compare.Sign, rhs: Expr[T]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = sign match {
      case Sign.`<`  => in < rhs.evaluate
      case Sign.`<=` => in <= rhs.evaluate
      case Sign.`>`  => in > rhs.evaluate
      case Sign.`>=` => in >= rhs.evaluate
    }

    override def renderExpression: String = (sign match {
      case Sign.`<`  => "<"
      case Sign.`<=` => "<="
      case Sign.`>`  => ">"
      case Sign.`>=` => ">="
    }) + s" ${rhs.renderExpression}"
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

  // Intervals are not supported yet

  case class Or[T](parts: Seq[UnaryTest[T]]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = parts.exists(_.evaluate(in))
    override def renderExpression: String = parts.map(_.renderExpression).mkString(", ")
  }
  case class Not[I](inner: UnaryTest[I])     extends UnaryTest[I] {
    override def evaluate(in: I): Boolean = !inner.evaluate(in)
    override def renderExpression: String = s"not(${inner.renderExpression})"
  }

  case class Bool[T](expr: Expr[Boolean]) extends UnaryTest[T] {
    override def evaluate(in: T): Boolean = expr.evaluate
    override def renderExpression: String = expr.renderExpression
  }

  extension [I](lhs: UnaryTest[I]) {
    def unary_! : UnaryTest[I]              = Not(lhs)
    def ||(rhs: UnaryTest[I]): UnaryTest[I] = Or(Seq(lhs, rhs))
  }
}

trait LowPriorityUnaryTestConversion {
  implicit def bool[T](expr: Expr[Boolean]): UnaryTest[T] = Bool(expr)
}
