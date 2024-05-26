package decisions4s.exprs

import decisions4s.exprs.TestUtils.checkExpression
import munit.FunSuite

class AndTest extends FunSuite {
  class AndTest extends FunSuite {
    test("basic") {
      checkExpression(And(True, True), true)
      checkExpression(True and True, true)
      checkExpression(True && True, true)

      checkExpression(And(True, False), false)
      checkExpression(True and False, false)
      checkExpression(True && False, false)

      checkExpression(And(False, True), false)
      checkExpression(False and True, false)
      checkExpression(False && True, false)

      checkExpression(And(False, False), false)
      checkExpression(False and False, false)
      checkExpression(False && False, false)
    }
  }
}
