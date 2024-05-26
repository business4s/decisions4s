package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class LessThanEqualTest extends FunSuite {
  test("basic") {
    checkExpression(LessThanEqual(Literal(1), Literal(2)), true)
    checkExpression(Literal(1) <= Literal(2), true)

    checkExpression(LessThanEqual(Literal(2), Literal(1)), false)
    checkExpression(Literal(2) <= Literal(1), false)

    checkExpression(LessThanEqual(Literal(2), Literal(2)), true)
    checkExpression(Literal(2) <= Literal(2), true)
  }
}
