package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class InTest extends FunSuite {
  test("basic") {
    checkExpression(In(Literal(1), Literal(1)), true)
    checkExpression(Literal(1) in Literal(1), true)

    checkExpression(In(Literal(2), Literal(1)), false)
    checkExpression(Literal(2) in Literal(1), false)
  }
}
