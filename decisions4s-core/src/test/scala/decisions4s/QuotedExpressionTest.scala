package decisions4s

import org.scalatest.freespec.AnyFreeSpec
import decisions4s.Expr.quoted

class QuotedExpressionTest extends AnyFreeSpec {

  "quoted" - {
    "should capture simple expression" in {
      val e = quoted(1 + 2)
      assert(e.evaluate == 3)
      assert(e.renderExpression == "1 + 2")
    }

    "should capture variable usage" in {
      val x = 10
      val e = quoted(x * 2)
      assert(e.evaluate == 20)
      assert(e.renderExpression == "x * 2")
    }
    
     "should capture function calls" in {
      def f(i: Int) = i + 1
      val e = quoted(f(10))
      assert(e.evaluate == 11)
      assert(e.renderExpression == "f(10)")
    }
  }
}
