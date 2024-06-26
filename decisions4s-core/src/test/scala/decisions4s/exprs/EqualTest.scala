package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class EqualTest extends FunSuite {
  test("basic") {
    checkExpression(Equal(Literal(1), Literal(1)), true)
    checkExpression(Literal(1) === Literal(1), true)

    checkExpression(Equal(Literal(1), Literal(2)), false)
    checkExpression(Literal(1) === Literal(2), false)
  }
}
