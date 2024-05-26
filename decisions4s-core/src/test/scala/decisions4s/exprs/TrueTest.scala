package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class TrueTest extends FunSuite {
  test("true"){
    checkExpression(True, true)
  }
}
