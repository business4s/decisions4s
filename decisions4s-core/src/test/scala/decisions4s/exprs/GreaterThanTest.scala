package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class GreaterThanTest extends FunSuite {
  test("basic") {
    checkExpression(GreaterThan(Literal(2), Literal(1)), true)
    checkExpression(Literal(2) > Literal(1), true)

    checkExpression(GreaterThan(Literal(1), Literal(2)), false)
    checkExpression(Literal(1) > Literal(2), false)

    checkExpression(GreaterThan(Literal(2), Literal(2)), false)
    checkExpression(Literal(2) > Literal(2), false)
  }
}
