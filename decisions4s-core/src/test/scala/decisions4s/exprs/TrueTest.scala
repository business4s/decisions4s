package decisions4s.exprs

import decisions4s.testing.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class TrueTest extends AnyFreeSpec {
  "true" in {
    checkExpression(True, true)
  }
}
