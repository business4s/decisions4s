package decisions4s.exprs

import decisions4s.testing.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class GreaterThanEqualTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(GreaterThanEqual(Literal(2), Literal(1)), true)
    checkExpression(Literal(2) >= Literal(1), true)

    checkExpression(GreaterThanEqual(Literal(1), Literal(2)), false)
    checkExpression(Literal(1) >= Literal(2), false)

    checkExpression(GreaterThanEqual(Literal(2), Literal(2)), true)
    checkExpression(Literal(2) >= Literal(2), true)
  }
}
