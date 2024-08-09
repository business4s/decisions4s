package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class PlusTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(Plus(Literal(1), Literal(2)), 3)
    checkExpression(Literal(1) + Literal(2), 3)
  }
}
