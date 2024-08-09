package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class FalseTest extends AnyFreeSpec {
  "basic" in {
    checkExpression(False, false)
  }
}
