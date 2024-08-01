package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkUnaryExpression
import decisions4s.it
import munit.FunSuite

class InputTest extends FunSuite {
  test("basic") {
    checkUnaryExpression(it.satisfies[Int](_ === Literal(1)), 1, true)
    checkUnaryExpression(it.satisfies[Int](_ === Literal(1)), 2, false)
  }
}
