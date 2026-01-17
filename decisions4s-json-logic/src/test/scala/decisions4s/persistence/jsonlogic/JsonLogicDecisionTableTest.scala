package decisions4s.persistence.jsonlogic

import decisions4s.persistence.DecisionTableDTO
import decisions4s.{HKD, HitPolicy, Value}
import org.scalatest.freespec.AnyFreeSpec

class JsonLogicDecisionTableTest extends AnyFreeSpec {

  "JsonLogicDecisionTable.load" - {

    "should bind input field names as json-logic variables" in {
      case class Input[F[_]](price: F[Int], quantity: F[Int]) derives HKD
      case class Output[F[_]](total: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map(
            "price"    -> """{">":[{"var":"price"}, 0]}""",
            "quantity" -> """{">":[{"var":"quantity"}, 0]}""",
          ),
          Map("total" -> """{"*":[{"var":"price"}, {"var":"quantity"}]}"""),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input[Value](10, 5))
      assert(result.output == Some(Output[Value](50)))
    }

    "should handle multiple input types (String, Int, Boolean)" in {
      case class Input[F[_]](name: F[String], age: F[Int], active: F[Boolean]) derives HKD
      case class Output[F[_]](eligible: F[Boolean]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map(
            // json-logic uses truthiness - non-empty string is truthy
            "name"   -> """{"!!":[{"var":"name"}]}""",
            "age"    -> """{">=":[{"var":"age"}, 18]}""",
            "active" -> """{"==":[{"var":"active"}, true]}""",
          ),
          Map("eligible" -> "true"),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("John", 25, true)).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("John", 17, true)).output == None)  // age < 18
      assert(table.evaluateFirst(Input("John", 25, false)).output == None) // not active
    }

    "should wire output expressions that compute from multiple input fields" in {
      case class Input[F[_]](base: F[Int], multiplier: F[Double]) derives HKD
      case class Output[F[_]](result: F[Double]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map(
            "base"       -> """{">":[{"var":"base"}, 0]}""",
            "multiplier" -> """{">":[{"var":"multiplier"}, 0]}""",
          ),
          Map("result" -> """{"*":[{"var":"base"}, {"var":"multiplier"}]}"""),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(100, 1.5))
      assert(result.output == Some(Output[Value](150.0)))
    }

    "should handle multiple output fields computed from the same inputs" in {
      case class Input[F[_]](a: F[Int], b: F[Int]) derives HKD
      case class Output[F[_]](sum: F[Int], diff: F[Int], product: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map(
            "a" -> """{">":[{"var":"a"}, 0]}""",
            "b" -> """{">":[{"var":"b"}, 0]}""",
          ),
          Map(
            "sum"     -> """{"+":[{"var":"a"}, {"var":"b"}]}""",
            "diff"    -> """{"-":[{"var":"a"}, {"var":"b"}]}""",
            "product" -> """{"*":[{"var":"a"}, {"var":"b"}]}""",
          ),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      val result = table.evaluateFirst(Input(10, 3))
      assert(result.output == Some(Output[Value](13, 7, 30)))
    }

    "should handle Int input and output" in {
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](doubled: F[Int]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map("x" -> """{">":[{"var":"x"}, 0]}"""),
          Map("doubled" -> """{"*":[{"var":"x"}, 2]}"""),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(21)).output == Some(Output[Value](42)))
      assert(table.evaluateFirst(Input(-5)).output == None) // condition not met
    }

    "should handle String input and output" in {
      case class Input[F[_]](prefix: F[String], name: F[String]) derives HKD
      case class Output[F[_]](greeting: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(DecisionTableDTO.Rule(
          Map(
            // json-logic uses truthiness - non-empty string is truthy
            "prefix" -> """{"!!":[{"var":"prefix"}]}""",
            "name"   -> """{"!!":[{"var":"name"}]}""",
          ),
          Map("greeting" -> """{"cat":[{"var":"prefix"}, {"var":"name"}]}"""),
          None,
        )),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("Hello, ", "World")).output == Some(Output[Value]("Hello, World")))
      assert(table.evaluateFirst(Input("Hi ", "")).output == None) // empty name
    }

    "should load multiple rules and evaluate them in order" in {
      case class Input[F[_]](x: F[Int]) derives HKD
      case class Output[F[_]](category: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(Map("x" -> """{">=":[{"var":"x"}, 100]}"""), Map("category" -> """"large""""), None),
          DecisionTableDTO.Rule(Map("x" -> """{">=":[{"var":"x"}, 10]}"""), Map("category" -> """"medium""""), None),
          DecisionTableDTO.Rule(Map("x" -> """{">=":[{"var":"x"}, 0]}"""), Map("category" -> """"small""""), None),
        ),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(150)).output == Some(Output[Value]("large")))
      assert(table.evaluateFirst(Input(50)).output == Some(Output[Value]("medium")))
      assert(table.evaluateFirst(Input(5)).output == Some(Output[Value]("small")))
      assert(table.evaluateFirst(Input(-1)).output == None)
    }

    "should support json-logic conditional (if)" in {
      case class Input[F[_]](score: F[Int]) derives HKD
      case class Output[F[_]](grade: F[String]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(
            Map("score" -> """{"and":[{">=":[{"var":"score"}, 90]}, {"<=":[{"var":"score"}, 100]}]}"""),
            Map("grade" -> """"A""""),
            None,
          ),
          DecisionTableDTO.Rule(
            Map("score" -> """{"and":[{">=":[{"var":"score"}, 80]}, {"<":[{"var":"score"}, 90]}]}"""),
            Map("grade" -> """"B""""),
            None,
          ),
          DecisionTableDTO.Rule(
            Map("score" -> """{"and":[{">=":[{"var":"score"}, 70]}, {"<":[{"var":"score"}, 80]}]}"""),
            Map("grade" -> """"C""""),
            None,
          ),
          DecisionTableDTO.Rule(
            Map("score" -> """{"<":[{"var":"score"}, 70]}"""),
            Map("grade" -> """"F""""),
            None,
          ),
        ),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input(95)).output == Some(Output[Value]("A")))
      assert(table.evaluateFirst(Input(85)).output == Some(Output[Value]("B")))
      assert(table.evaluateFirst(Input(75)).output == Some(Output[Value]("C")))
      assert(table.evaluateFirst(Input(65)).output == Some(Output[Value]("F")))
    }

    "should support json-logic in (list membership)" in {
      case class Input[F[_]](status: F[String]) derives HKD
      case class Output[F[_]](allowed: F[Boolean]) derives HKD

      val dto = DecisionTableDTO(
        Seq(
          DecisionTableDTO.Rule(
            Map("status" -> """{"in":[{"var":"status"}, ["active", "pending"]]}"""),
            Map("allowed" -> "true"),
            None,
          ),
          DecisionTableDTO.Rule(
            Map("status" -> """{"in":[{"var":"status"}, ["inactive", "blocked"]]}"""),
            Map("allowed" -> "false"),
            None,
          ),
        ),
        "test",
      )

      val table = JsonLogicDecisionTable
        .load[Input, Output, HitPolicy.First](dto, HKD.gatherGivens[Output, FromJsonLogic], HitPolicy.First)
        .get

      assert(table.evaluateFirst(Input("active")).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("pending")).output == Some(Output[Value](true)))
      assert(table.evaluateFirst(Input("inactive")).output == Some(Output[Value](false)))
      assert(table.evaluateFirst(Input("unknown")).output == None)
    }

  }

}
