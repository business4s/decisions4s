package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import decisions4s.testing.HiddenTripleEquals
import org.scalatest.freespec.AnyFreeSpec
class NotEqualTest extends AnyFreeSpec with HiddenTripleEquals {

  "basic" in {
    checkExpression(NotEqual(Literal(1), Literal(1)), false)
    checkExpression(Literal(1) !== Literal(1), false)

    checkExpression(NotEqual(Literal(1), Literal(2)), true)
    checkExpression(Literal(1) !== Literal(2), true)
  }

}
