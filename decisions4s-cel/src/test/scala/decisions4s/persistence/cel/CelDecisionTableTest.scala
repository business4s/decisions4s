package decisions4s.persistence.cel

import decisions4s.persistence.DecisionTableDTO
import decisions4s.{HKD, HitPolicy, Value}
import org.scalatest.freespec.AnyFreeSpec

class CelDecisionTableTest extends AnyFreeSpec {

  "CelDecisionTable.load" - {

    "should bind input field names as CEL variables" in {
      // Tests that field names from the case class become available as variables in CEL
      case class Input[F[_]](price: F[Int], quantity: F[Int]) derives HKD
      case class Output[F[_]](total: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("price" -> "true", "quantity" -> "true"),
          Map("total" -> "price * quantity"), // uses both field names as variables
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(10, 5))
      assert(result.output == Some(Output[Value](50)))
    }

    "should handle multiple input types (String, Int, Boolean)" in {
      case class Input[F[_]](name: F[String], age: F[Int], active: F[Boolean]) derives HKD
      case class Output[F[_]](eligible: F[Boolean]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("name" -> "size(name) > 0", "age" -> "age >= 18", "active" -> "active"),
          Map("eligible" -> "true"),
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
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
          Map("base" -> "true", "multiplier" -> "true"),
          Map("result" -> "double(base) * multiplier"),
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(100, 1.5))
      assert(result.output == Some(Output[Value](150.0)))
    }

    "should handle multiple output fields computed from same inputs" in {
      case class Input[F[_]](a: F[Int], b: F[Int]) derives HKD
      case class Output[F[_]](sum: F[Int], diff: F[Int], product: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("a" -> "true", "b" -> "true"),
          Map("sum" -> "a + b", "diff" -> "a - b", "product" -> "a * b"),
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(10, 3))
      assert(result.output == Some(Output[Value](13, 7, 30)))
    }

    "should convert Int input to Long for CEL and back to Int for output" in {
      // CEL uses 64-bit integers (Long), this tests the ToCelType/FromCel conversion
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](doubled: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("x" -> "x > 0"),
          Map("doubled" -> "x * 2"),
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(21)).output == Some(Output[Value](42)))
      assert(table.evaluateFirst(Input(-5)).output == None) // condition not met
    }

    "should handle String input and output" in {
      case class Input[F[_]](prefix: F[String], name: F[String]) derives HKD
      case class Output[F[_]](greeting: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("prefix" -> "true", "name" -> "size(name) > 0"),
          Map("greeting" -> "prefix + name"),
          None,
        )),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("Hello, ", "World")).output == Some(Output[Value]("Hello, World")))
      assert(table.evaluateFirst(Input("Hi ", "")).output == None) // empty name
    }

    "should load multiple rules and evaluate them in order" in {
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](category: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(Map("x" -> "x >= 100"), Map("category" -> "\"large\""), None),
          DecisionTableDTO.Rule(Map("x" -> "x >= 10"), Map("category" -> "\"medium\""), None),
          DecisionTableDTO.Rule(Map("x" -> "x >= 0"), Map("category" -> "\"small\""), None),
        ),
        "test",
      )

      val table = CelDecisionTable
        .load(dto, HKD.gatherGivens[Input, ToCelType], HKD.gatherGivens[Output, FromCel], HitPolicy.First)
        .get

      // First hit policy: first matching rule wins
      assert(table.evaluateFirst(Input(150)).output == Some(Output[Value]("large")))
      assert(table.evaluateFirst(Input(50)).output == Some(Output[Value]("medium")))
      assert(table.evaluateFirst(Input(5)).output == Some(Output[Value]("small")))
      assert(table.evaluateFirst(Input(-1)).output == None)
    }
  }

}
