package decisions4s.persistence.feel

import decisions4s.persistence.DecisionTableDTO
import decisions4s.{HKD, HitPolicy, Value}
import org.scalatest.freespec.AnyFreeSpec

class FeelDecisionTableTest extends AnyFreeSpec {

  "FeelDecisionTable.load" - {

    "should bind input field names as FEEL variables" in {
      case class Input[F[_]](price: F[Int], quantity: F[Int]) derives HKD
      case class Output[F[_]](total: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("price" -> "> 0", "quantity" -> "> 0"),
          Map("total" -> "price * quantity"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input[Value](10, 5))
      assert(result.output == Some(Output[Value](50)))
    }

    "should handle multiple input types (String, Int, Boolean)" in {
      case class Input[F[_]](name: F[String], age: F[Int], active: F[Boolean]) derives HKD
      case class Output[F[_]](eligible: F[Boolean]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          // FEEL unary tests: ? references the input value
          Map("name" -> "string length(?) > 0", "age" -> ">= 18", "active" -> "true"),
          Map("eligible" -> "true"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("John", 25, true)).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("John", 17, true)).output == None)  // age < 18
      assert(table.evaluateFirst(Input("", 25, true)).output == None)      // empty name
      assert(table.evaluateFirst(Input("John", 25, false)).output == None) // not active
    }

    "should wire output expressions that compute from multiple input fields" in {
      case class Input[F[_]](base: F[Int], multiplier: F[Double]) derives HKD
      case class Output[F[_]](result: F[Double]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("base" -> "> 0", "multiplier" -> "> 0"),
          Map("result" -> "base * multiplier"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(100, 1.5))
      assert(result.output == Some(Output[Value](150.0)))
    }

    "should handle multiple output fields computed from same inputs" in {
      case class Input[F[_]](a: F[Int], b: F[Int]) derives HKD
      case class Output[F[_]](sum: F[Int], diff: F[Int], product: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("a" -> "> 0", "b" -> "> 0"),
          Map("sum" -> "a + b", "diff" -> "a - b", "product" -> "a * b"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(10, 3))
      assert(result.output == Some(Output[Value](13, 7, 30)))
    }

    "should handle Int input and output with FEEL number conversion" in {
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](doubled: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("x" -> "> 0"),
          Map("doubled" -> "x * 2"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(21)).output == Some(Output[Value](42)))
      assert(table.evaluateFirst(Input(-5)).output == None) // condition not met
    }

    "should handle String input and output" in {
      case class Input[F[_]](prefix: F[String], name: F[String]) derives HKD
      case class Output[F[_]](greeting: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          // FEEL unary tests: ? references the input value
          Map("prefix" -> "string length(?) > 0", "name" -> "string length(?) > 0"),
          Map("greeting" -> "prefix + name"),
          None,
        )),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("Hello, ", "World")).output == Some(Output[Value]("Hello, World")))
      assert(table.evaluateFirst(Input("Hi ", "")).output == None) // empty name
    }

    "should load multiple rules and evaluate them in order" in {
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](category: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(Map("x" -> ">= 100"), Map("category" -> "\"large\""), None),
          DecisionTableDTO.Rule(Map("x" -> ">= 10"), Map("category" -> "\"medium\""), None),
          DecisionTableDTO.Rule(Map("x" -> ">= 0"), Map("category" -> "\"small\""), None),
        ),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(150)).output == Some(Output[Value]("large")))
      assert(table.evaluateFirst(Input(50)).output == Some(Output[Value]("medium")))
      assert(table.evaluateFirst(Input(5)).output == Some(Output[Value]("small")))
      assert(table.evaluateFirst(Input(-1)).output == None)
    }

    "should support FEEL range expressions" in {
      case class Input[F[_]](score: F[Int]) derives HKD
      case class Output[F[_]](grade: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(Map("score" -> "[90..100]"), Map("grade" -> "\"A\""), None),
          DecisionTableDTO.Rule(Map("score" -> "[80..90)"), Map("grade" -> "\"B\""), None),
          DecisionTableDTO.Rule(Map("score" -> "[70..80)"), Map("grade" -> "\"C\""), None),
          DecisionTableDTO.Rule(Map("score" -> "< 70"), Map("grade" -> "\"F\""), None),
        ),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(95)).output == Some(Output[Value]("A")))
      assert(table.evaluateFirst(Input(85)).output == Some(Output[Value]("B")))
      assert(table.evaluateFirst(Input(75)).output == Some(Output[Value]("C")))
      assert(table.evaluateFirst(Input(65)).output == Some(Output[Value]("F")))
    }

    "should support FEEL disjunction (list of values)" in {
      case class Input[F[_]](status: F[String]) derives HKD
      case class Output[F[_]](allowed: F[Boolean]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(Map("status" -> "\"active\", \"pending\""), Map("allowed" -> "true"), None),
          DecisionTableDTO.Rule(Map("status" -> "\"inactive\", \"blocked\""), Map("allowed" -> "false"), None),
        ),
        "test",
      )

      val table = FeelDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromFeel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("active")).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("pending")).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("inactive")).output == Some(Output[Value](false)))
      assert(table.evaluateFirst(Input("unknown")).output == None)
    }

  }

}
