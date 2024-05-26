package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class LessThanTest extends FunSuite {
  test("basic") {
    checkExpression(LessThan(Literal(1), Literal(2)), true)
    checkExpression(Literal(1) < Literal(2), true)

    checkExpression(LessThan(Literal(2), Literal(1)), false)
    checkExpression(Literal(2) < Literal(1), false)

    checkExpression(LessThan(Literal(1), Literal(1)), false)
    checkExpression(Literal(1) < Literal(1), false)
  }
}
