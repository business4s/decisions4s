package decisions4s.exprs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import decisions4s.exprs.TestUtils.checkExpression

class ExampleSpec extends AnyFlatSpec with Matchers {
  "AndTest" should "basic" in {
    assert(And(True, True).evaluate(()) == true)
    assert((True and True).evaluate(()) == true)
    assert((True && True).evaluate(()) == true)
    assert(And(True, False).evaluate(()) == false)
    assert((True and False).evaluate(()) == false)
    assert((True && False).evaluate(()) == false)
    assert(And(False, True).evaluate(()) == false)
    assert((False and True).evaluate(()) == false)
    assert((False && True).evaluate(()) == false)
    assert(And(False, False).evaluate(()) == false)
    assert((False and False).evaluate(()) == false)
    assert((False && False).evaluate(()) == false)
  }
}
