package decisions4s.exprs

import org.scalatest.freespec.AnyFreeSpec

import decisions4s.*

class FunctionTest extends AnyFreeSpec {

  "function1" in {
    val f = Function[Int]("addOne")(1.asLiteral)(_ + 1)
    assert(f.evaluate == 2)
    assert(f.renderExpression == "addOne(1)")
  }
  "function2" in {
    val f = Function[Int]("add")(1.asLiteral, 2.asLiteral)(_ + _ + 1)
    assert(f.evaluate == 4)
    assert(f.renderExpression == "add(1, 2)")
  }
  "function3" in {
    val f = Function[Int]("add")(1.asLiteral, 2.asLiteral, 3.asLiteral)(_ + _ + _ )
    assert(f.evaluate == 6)
    assert(f.renderExpression == "add(1, 2, 3)")
  }
  "autonamed" in {
    val f123a = Function.autoNamed[Int](1.asLiteral)(x => x)
    assert(f123a.evaluate == 1)
    assert(f123a.renderExpression == "f123a(1)")
  }

}
