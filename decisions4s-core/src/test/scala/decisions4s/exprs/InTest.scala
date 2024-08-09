package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import decisions4s.it
import org.scalatest.freespec.AnyFreeSpec
class InTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(In(Literal(1), it.equalsTo(1)), true)
    checkExpression(Literal(1) in it.equalsTo(1), true)

    checkExpression(In(Literal(2), it.equalsTo(1)), false)
    checkExpression(Literal(2) in it.equalsTo(1), false)
  }
}
