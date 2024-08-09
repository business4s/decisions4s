package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class MinusTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(Minus(Literal(1), Literal(2)), -1)
    checkExpression(Literal(1) - Literal(2), -1)
  }
}
