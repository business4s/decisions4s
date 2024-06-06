package decisions4s.example.docs

import decisions4s.exprs.UnaryTest
import decisions4s.{Expr, it}

object ExpressionsExample {

  // start_expr
  import decisions4s.it
  val lowerThan5: UnaryTest[Int]  = it < 5
  val equalFoo: UnaryTest[String] = it.equalsTo("foo")
  val complex: Expr[Int, Boolean] = it > 1 && it < 5
  // end_expr

  // start_custom_generic
  class EndsWithFoo[In](argument: Expr[In, String]) extends Expr[In, Boolean] {
    override def evaluate(in: In): Boolean = argument.evaluate(in).endsWith("foo")
    override def renderExpression: String  = s"endsWithFoo(${argument.renderExpression})"
  }
  val endsWithFoo: EndsWithFoo[String] = EndsWithFoo(it.value)
  // end_custom_generic

  // start_custom_simplified
  class EndsWithFooSimple extends Expr[String, Boolean] {
    override def evaluate(in: String): Boolean = in.endsWith("foo")
    override def renderExpression: String  = s"endsWithFoo()"
  }
  val endsWithFooSimple: EndsWithFoo[String] = EndsWithFoo(it.value)
  // end_custom_simplified

}
