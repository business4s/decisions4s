package decisions4s.exprs

import decisions4s.{Expr, it}
import decisions4s.exprs.TestUtils.*
import decisions4s.exprs.UnaryTest.{CatchAll, Not, Or}
import munit.FunSuite

class UnaryTestTest extends FunSuite {

  test("bool conversion") {
    val bool1: Expr[Any, Int] = Literal(1)
    val unary1: UnaryTest[Int]    = bool1
    checkUnaryExpression(unary1, 1, true)
    checkUnaryExpression(unary1, 2, false)
  }

  test("comparison with boolean takes precedence over boolean conversion") {
    checkUnaryExpression(UnaryTest.EqualTo(True), false, false)
    checkUnaryExpression(True, false, false)
  }

  test("list conversion") {
    val list: Expr[Int, List[Int]] = Literal(List(1, 2, 3))
    val unary: UnaryTest[Int]      = list
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(unary, 2, true)
    checkUnaryExpression(unary, 4, false)
  }
  test("value conversion") {
    val value: Expr[Int, Int] = Literal(1)
    val unary: UnaryTest[Int] = value
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(unary, 2, false)
  }
  test("catchAll") {
    checkUnaryExpression(CatchAll, 1, true)
    checkUnaryExpression(it.catchAll, 1, true)
  }
  test("comparison") {
    checkUnaryExpression(it > 1, 0, false)
    checkUnaryExpression(it > 1, 1, false)
    checkUnaryExpression(it > 1, 2, true)

    checkUnaryExpression(it >= 1, 0, false)
    checkUnaryExpression(it >= 1, 1, true)
    checkUnaryExpression(it >= 1, 2, true)

    checkUnaryExpression(it < 1, 0, true)
    checkUnaryExpression(it < 1, 1, false)
    checkUnaryExpression(it < 1, 2, false)

    checkUnaryExpression(it <= 1, 0, true)
    checkUnaryExpression(it <= 1, 1, true)
    checkUnaryExpression(it <= 1, 2, false)
  }
  test("or") {
    checkUnaryExpression((it < 10) || (it > 50), 9, true)
    checkUnaryExpression((it < 10) || (it > 50), 20, false)
    checkUnaryExpression((it < 10) || (it > 50), 51, true)
    checkUnaryExpression(Or(Seq(Literal(1), Literal(2))), 1, true)
    checkUnaryExpression(Or(Seq(Literal(1), Literal(2))), 2, true)
    checkUnaryExpression(Or(Seq(Literal(1), Literal(2))), 3, false)
  }
  test("negation") {
    checkUnaryExpression(Not(Literal(1)), 1, false)
    checkUnaryExpression(Not(Literal(1)), 2, true)

    checkUnaryExpression(Not(Or(Seq(Literal(1), Literal(2)))), 1, false)
    checkUnaryExpression(Not(Or(Seq(Literal(1), Literal(2)))), 2, false)
    checkUnaryExpression(Not(Or(Seq(Literal(1), Literal(2)))), 3, true)

    checkUnaryExpression(!(Literal(1): UnaryTest[Int]), 1, false)
  }

}
