package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class MultiplyTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(Multiply(Literal(3), Literal(4)), 12)
    checkExpression(Literal(3) * Literal(4), 12)
  }
}
