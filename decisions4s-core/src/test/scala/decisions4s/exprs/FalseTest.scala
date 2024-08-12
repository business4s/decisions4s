package decisions4s.exprs

import decisions4s.testing.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class FalseTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(False, false)
  }
}
