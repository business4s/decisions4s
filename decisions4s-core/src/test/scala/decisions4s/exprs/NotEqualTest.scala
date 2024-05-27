package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class NotEqualTest extends FunSuite {

  test("basic") {
    checkExpression(NotEqual(Literal(1), Literal(1)), false)
    checkExpression(Literal(1) !== Literal(1), false)

    checkExpression(NotEqual(Literal(1), Literal(2)), true)
    checkExpression(Literal(1) !== Literal(2), true)
  }

}
