package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class MultiplyTest extends FunSuite {
  test("basic") {
    checkExpression(Multiply(Literal(3), Literal(4)), 12)
    checkExpression(Literal(3) * Literal(4), 12)
  }
}
