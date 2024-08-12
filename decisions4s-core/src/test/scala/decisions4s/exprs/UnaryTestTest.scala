package decisions4s.exprs

import decisions4s.{Expr, it}
import decisions4s.testing.TestUtils.*
import decisions4s.exprs.UnaryTest.{CatchAll, Not, Or}
import org.scalatest.freespec.AnyFreeSpec
class UnaryTestTest extends AnyFreeSpec {

  "bool conversion" in {
    val bool1: Expr[Boolean]   = Literal(1) > 0
    val unary1: UnaryTest[Int] = bool1
    checkUnaryExpression(unary1, 2, true)
  }

  "comparison with boolean" in {
    checkUnaryExpression(UnaryTest.EqualTo(True), false, false)
    checkUnaryExpression(True: UnaryTest[Boolean], true, true)
    checkUnaryExpression(UnaryTest.EqualTo(False), false, true)
    checkUnaryExpression(False: UnaryTest[Boolean], true, false)
  }

  "list conversion" in {
    val list: Expr[List[Int]] = Literal(List(1, 2, 3))
    val unary: UnaryTest[Int] = it.equalsAnyOf(list)
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 1, true)
    checkUnaryExpression(unary, 2, true)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 2, true)
    checkUnaryExpression(unary, 4, false)
    checkUnaryExpression(it.equalsAnyOf(1, 2, 3), 4, false)
  }
  "value conversion" in {
    val value: Expr[Int]      = Literal(1)
    val unary: UnaryTest[Int] = it.equalsTo(value)
    checkUnaryExpression(unary, 1, true)
    checkUnaryExpression(it === 1, 1, true)

    checkUnaryExpression(unary, 2, false)
    checkUnaryExpression(it === 1, 2, false)
  }
  "catchAll" in {
    checkUnaryExpression(CatchAll, 1, true)
    checkUnaryExpression(it.catchAll, 1, true)
  }
  "comparison" in {
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
  "or" in {
    checkUnaryExpression((it < 10) || (it > 50), 9, true)
    checkUnaryExpression((it < 10) || (it > 50), 20, false)
    checkUnaryExpression((it < 10) || (it > 50), 51, true)
  }
  "negation" in {
    checkUnaryExpression(Not(it.equalsTo(1)), 1, false)
    checkUnaryExpression(Not(it.equalsTo(1)), 2, true)

    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 1, false)
    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 2, false)
    checkUnaryExpression(Not(Or(Seq(it.equalsTo(1), it.equalsTo(2)))), 3, true)

    checkUnaryExpression(!(it > 1), 2, false)
  }
  "boolean" in {
    checkUnaryExpression(it.isTrue, true, true)
    checkUnaryExpression(it.isTrue, false, false)
    checkUnaryExpression(it.isFalse, true, false)
    checkUnaryExpression(it.isFalse, false, true)
  }

}
