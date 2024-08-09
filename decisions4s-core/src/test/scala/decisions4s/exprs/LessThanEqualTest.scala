package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class LessThanEqualTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(LessThanEqual(Literal(1), Literal(2)), true)
    checkExpression(Literal(1) <= Literal(2), true)

    checkExpression(LessThanEqual(Literal(2), Literal(1)), false)
    checkExpression(Literal(2) <= Literal(1), false)

    checkExpression(LessThanEqual(Literal(2), Literal(2)), true)
    checkExpression(Literal(2) <= Literal(2), true)
  }
}
