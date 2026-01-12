package decisions4s.html

import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec

class HtmlRendererTest extends AnyFreeSpec {

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

  "HtmlRenderer" - {
    "should render a basic table" in {
      val html = HtmlRenderer.render(testTable)

      assert(html.contains("<!DOCTYPE html>"))
      assert(html.contains("test"))
      assert(html.contains("Inputs"))
      assert(html.contains("Outputs"))
      assert(html.contains("foo"))
      assert(html.contains("bar"))
      assert(html.contains("c"))
      assert(html.contains("Hit Policy: Single"))
      assert(html.contains("&gt; 3"))
      assert(html.contains("some string"))
    }

    "should handle annotations" in {
      val tableWithAnnotation: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
        rules = List(
          Rule(
            matching = Input(
              foo = it > 3,
              bar = it.equalsTo("some string"),
            ),
            output = Output(2),
            annotation = Some("this is a test rule"),
          ),
          Rule.default(Output(1)),
        ),
        "test_with_anno",
        HitPolicy.Single,
      )

      val html = HtmlRenderer.render(tableWithAnnotation)
      assert(html.contains("Annotations"))
      assert(html.contains("this is a test rule"))
    }
  }
}
