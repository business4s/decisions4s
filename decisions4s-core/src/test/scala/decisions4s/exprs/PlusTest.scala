package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class PlusTest extends FunSuite {
  test("basic") {
    checkExpression(Plus(Literal(1), Literal(2)), 3)
    checkExpression(Literal(1) + Literal(2), 3)
  }
}
