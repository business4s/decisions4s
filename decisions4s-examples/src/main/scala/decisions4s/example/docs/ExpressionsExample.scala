package decisions4s.example.docs

import decisions4s.exprs.{Literal, UnaryTest}

import scala.annotation.nowarn

@nowarn("msg=unused value")
object ExpressionsExample {

  // start_expr
  import decisions4s.*

  val a: Expr[Int]     = 1.asLiteral
  val b: Expr[Boolean] = a > 0

  val lowerThan5: UnaryTest[Int]  = it < 5
  val equalFoo: UnaryTest[String] = it.equalsTo("foo")
  val complex: UnaryTest[Int]     = it.satisfies(v => v > 1 && v < 5)
  // end_expr

  // start_custom_generic
  case class EndsWithFoo(argument: Expr[String]) extends Expr[Boolean] {
    override def evaluate: Boolean        = argument.evaluate.endsWith("foo")
    override def renderExpression: String = s"endsWithFoo(${argument.renderExpression})"
  }
  val endsWithFoo: UnaryTest[String] = it.satisfies(EndsWithFoo.apply)
  val endsWithFoo2: Expr[Boolean] = EndsWithFoo(Literal("myfoo"))
  // end_custom_generic

  {
    // start_custom_streamlined
    extension (arg1: Expr[String]) {
      def endsWith(arg2: Expr[String]) = Function.autoNamed[Boolean](arg1, arg2)(_.endsWith(_))
    }
    val endsWithFoo: UnaryTest[String] = it.satisfies(_.endsWith("foo".asLiteral))
    // end_custom_streamlined
    endsWithFoo.toString // to bypass not used
  }
}
