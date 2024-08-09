package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import decisions4s.testing.HiddenTripleEquals
import org.scalatest.freespec.AnyFreeSpec
class EqualTest extends AnyFreeSpec with HiddenTripleEquals {

  "basic" in {
    checkExpression(Equal(Literal(1), Literal(1)), true)
    checkExpression(Literal(1) === Literal(1), true)
    checkExpression(Literal(1) === 1, true)

    checkExpression(Equal(Literal(1), Literal(2)), false)
    checkExpression(Literal(1) === Literal(2), false)
    checkExpression(Literal(1) === 2, false)
  }
}
