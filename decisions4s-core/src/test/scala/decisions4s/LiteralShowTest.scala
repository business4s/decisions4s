package decisions4s

import org.scalatest.freespec.AnyFreeSpec

class LiteralShowTest extends AnyFreeSpec {

  "summon" in {
    assert(LiteralShow[Int] == summon[LiteralShow[Int]])
  }
  "showAsLiteral" in {
    assert(1.showAsLiteral == summon[LiteralShow[Int]].show(1))
  }
}
