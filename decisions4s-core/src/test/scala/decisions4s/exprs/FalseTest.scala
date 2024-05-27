package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class FalseTest extends FunSuite {
  test("basic") {
    checkExpression(False, false)
  }
}
