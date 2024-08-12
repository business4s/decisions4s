package decisions4s.exprs

import decisions4s.testing.TestUtils.checkExpression
import org.scalatest.freespec.AnyFreeSpec

class AndTest extends AnyFreeSpec {
  "basic" in {
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
