package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkUnaryExpression
import decisions4s.it
import decisions4s.testing.HiddenTripleEquals
import org.scalatest.freespec.AnyFreeSpec
class InputTest extends AnyFreeSpec with HiddenTripleEquals {
  import org.scalatest.Assertions.{=== => _}
  "basic" in {
    checkUnaryExpression(it.satisfies[Int](_ === Literal(1)), 1, true)
    checkUnaryExpression(it.satisfies[Int](_ === Literal(1)), 2, false)
  }
}
