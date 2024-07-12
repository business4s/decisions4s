package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class MinusTest extends FunSuite {
  test("basic") {
    checkExpression(Minus(Literal(1), Literal(2)), -1)
    checkExpression(Literal(1) - Literal(2), -1)
  }
}
