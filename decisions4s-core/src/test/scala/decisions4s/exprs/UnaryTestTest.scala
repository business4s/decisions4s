package decisions4s.exprs

import decisions4s.{Expr, it}
import decisions4s.exprs.TestUtils.*
import decisions4s.exprs.UnaryTest.{CatchAll, Not, Or}
import munit.FunSuite

class UnaryTestTest extends FunSuite {

  test("bool conversion") {
    val bool1: Expr[Boolean]   = Literal(1) > 0
    val unary1: UnaryTest[Int] = bool1
    checkUnaryExpression(unary1, 2, true)
  }

  test("comparison with boolean") {
    checkUnaryExpression(UnaryTest.EqualTo(True), false, false)
    checkUnaryExpression(True: UnaryTest[Boolean], true, true)
    checkUnaryExpression(UnaryTest.EqualTo(False), false, true)
    checkUnaryExpression(False: UnaryTest[Boolean], true, false)
  }

  test("list conversion") {
    val list: Expr[List[Int]] = Literal(List(1, 2, 3))
    val unary: UnaryTest[Int] = it.equalsAnyOf(list)
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 1, true)
    checkUnaryExpression(unary, 2, true)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 2, true)
    checkUnaryExpression(unary, 4, false)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 4, false)
  }
  test("value conversion") {
    val value: Expr[Int]      = Literal(1)
    val unary: UnaryTest[Int] = it.equalsTo(value)
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(it === 1, 1, true)

    checkUnaryExpression(unary, 2, false)
    checkUnaryExpression(it === 1, 2, false)
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
  }
  test("negation") {
    checkUnaryExpression(Not(it.equalsTo(1)), 1, false)
    checkUnaryExpression(Not(it.equalsTo(1)), 2, true)

    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 1, false)
    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 2, false)
    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 3, true)

    checkUnaryExpression(!(it > 1), 2, false)
  }

}
