package decisions4s.markdown

import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec

class MarkdownRendererTest extends AnyFreeSpec {

  case class Input[F[_]](a: F[Int]) derives HKD
  case class Output[F[_]](c: F[Int]) derives HKD

  val testTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(it > 3),
        output = Output(2),
      ),
      Rule(
        matching = Input(it > 2),
        output = Output(1),
      ),
    ),
    "test",
    HitPolicy.Single,
  )

  "basic" in {

    println(MarkdownRenderer.render(testTable))

  }

}
