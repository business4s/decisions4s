package decisions4s.exprs

import decisions4s.exprs.TestUtils.{checkExpression, checkUnaryExpression}
import decisions4s.it
import munit.FunSuite

class InputTest extends FunSuite {
  test("basic") {
    checkUnaryExpression(it.value[Int] === Literal(1), 1, true)
    checkUnaryExpression(it.value[Int] === Literal(1), 2, false)
  }
}
