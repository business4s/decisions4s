package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class BetweenTest extends FunSuite {
  test("basic") {
    checkExpression(Between(Literal(2), Literal(1), Literal(3)), true)
    checkExpression(Literal(2).between(Literal(1), Literal(3)), true)

    checkExpression(Between(Literal(1), Literal(2), Literal(3)), false)
    checkExpression(Literal(1).between(Literal(2), Literal(3)), false)

    checkExpression(Between(Literal(3), Literal(1), Literal(3)), true)
    checkExpression(Literal(3).between(Literal(1), Literal(3)), true)

    checkExpression(Between(Literal(0), Literal(1), Literal(3)), false)
    checkExpression(Literal(0).between(Literal(1), Literal(3)), false)
  }
}
