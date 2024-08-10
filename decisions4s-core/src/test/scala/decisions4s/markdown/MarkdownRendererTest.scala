package decisions4s.markdown

import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec

class MarkdownRendererTest extends AnyFreeSpec {

  case class Input[F[_]](foo: F[Int], bar: F[String]) derives HKD
  case class Output[F[_]](c: F[Int]) derives HKD

  val testTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(
          foo = it > 3,
          bar = it.equalsTo("some string"),
        ),
        output = Output(2),
      ),
      Rule.default(Output(1)),
    ),
    "test",
    HitPolicy.Single,
  )

  "basic" in {

    println(MarkdownRenderer.render(testTable))

  }

}
