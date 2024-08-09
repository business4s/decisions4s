package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec
class TrueTest extends AnyFreeSpec {
  "true" in {
    checkExpression(True, true)
  }
}
