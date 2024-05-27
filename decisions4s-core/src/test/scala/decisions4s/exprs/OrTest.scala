package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class OrTest extends FunSuite {
  test("basic") {
    checkExpression(Or(True, True), true)
    checkExpression(True or True, true)
    checkExpression(True || True, true)

    checkExpression(Or(True, False), true)
    checkExpression(True or False, true)
    checkExpression(True || False, true)

    checkExpression(Or(False, True), true)
    checkExpression(False or True, true)
    checkExpression(False || True, true)

    checkExpression(Or(False, False), false)
    checkExpression(False or False, false)
    checkExpression(False || False, false)
  }
}
