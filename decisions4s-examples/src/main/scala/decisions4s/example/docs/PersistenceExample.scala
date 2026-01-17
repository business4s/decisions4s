package decisions4s.example.docs

import scala.annotation.nowarn

@nowarn("msg=unused value")
object PersistenceExample {

  // start_dto
  import decisions4s.persistence.DecisionTableDTO
  import io.circe.parser.decode

  val json = """
  {
    "name": "pricing",
    "rules": [
      {
        "inputs": { "price": "> 100", "quantity": ">= 10" },
        "outputs": { "discount": "0.1" },
        "annotation": "Bulk discount"
      },
      {
        "inputs": { "price": "> 0", "quantity": "> 0" },
        "outputs": { "discount": "0.0" },
        "annotation": "No discount"
      }
    ]
  }
  """

  val dto: DecisionTableDTO = decode[DecisionTableDTO](json).toTry.get
  // end_dto

  // start_cel
  import decisions4s.*
  import decisions4s.persistence.cel.*

  case class PricingInput[F[_]](price: F[Int], quantity: F[Int]) derives HKD
  case class PricingOutput[F[_]](discount: F[Double]) derives HKD

  val celDto = DecisionTableDTO(
    Seq(
      DecisionTableDTO.Rule(
        Map("price" -> "price > 100", "quantity" -> "quantity >= 10"),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map("price" -> "true", "quantity" -> "true"),
        Map("discount" -> "0.0"),
        Some("No discount"),
      ),
    ),
    "pricing",
  )

  val celTable = CelDecisionTable
    .load(
      celDto,
      HKD.gatherGivens[PricingInput, ToCelType],
      HKD.gatherGivens[PricingOutput, FromCel],
      HitPolicy.First,
    )
    .get

  val celResult = celTable.evaluateFirst(PricingInput(150, 20))
  // celResult.output == Some(PricingOutput[Value](0.1))
  // end_cel

  // start_feel
  import decisions4s.persistence.feel.*

  val feelDto = DecisionTableDTO(
    Seq(
      DecisionTableDTO.Rule(
        // FEEL unary tests - input value referenced via ?
        Map("price" -> "> 100", "quantity" -> ">= 10"),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map("price" -> "> 0", "quantity" -> "> 0"),
        Map("discount" -> "0.0"),
        Some("No discount"),
      ),
    ),
    "pricing",
  )

  val feelTable = FeelDecisionTable
    .load[PricingInput, PricingOutput, HitPolicy.First](
      feelDto,
      HKD.gatherGivens[PricingOutput, FromFeel],
      HitPolicy.First,
    )
    .get

  val feelResult = feelTable.evaluateFirst(PricingInput(150, 20))
  // feelResult.output == Some(PricingOutput[Value](0.1))
  // end_feel

  // start_feel_unary
  // FEEL supports native unary test syntax:
  // - Comparisons: > 100, >= 10, < 5, <= 0, = "active"
  // - Ranges: [1..10], (0..100], [0..100)
  // - Lists: "A", "B", "C" (matches if input is any of these)
  // - Negation: not("inactive", "blocked")
  // - Any: - (matches any value)
  // end_feel_unary

  // start_jsonlogic
  import decisions4s.persistence.jsonlogic.*

  val jsonLogicDto = DecisionTableDTO(
    Seq(
      DecisionTableDTO.Rule(
        // json-logic uses JSON objects for expressions
        Map(
          "price"    -> """{">":[{"var":"price"}, 100]}""",
          "quantity" -> """{">=":[{"var":"quantity"}, 10]}""",
        ),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map(
          "price"    -> """{">":[{"var":"price"}, 0]}""",
          "quantity" -> """{">":[{"var":"quantity"}, 0]}""",
        ),
        Map("discount" -> "0.0"),
        Some("No discount"),
      ),
    ),
    "pricing",
  )

  val jsonLogicTable = JsonLogicDecisionTable
    .load[PricingInput, PricingOutput, HitPolicy.First](
      jsonLogicDto,
      HKD.gatherGivens[PricingOutput, FromJsonLogic],
      HitPolicy.First,
    )
    .get

  val jsonLogicResult = jsonLogicTable.evaluateFirst(PricingInput(150, 20))
  // jsonLogicResult.output == Some(PricingOutput[Value](0.1))
  // end_jsonlogic

  // start_jsonlogic_syntax
  // json-logic syntax examples:
  // - Comparisons: {">":[{"var":"x"}, 100]}, {">=":[{"var":"x"}, 10]}
  // - Boolean logic: {"and":[...]}, {"or":[...]}, {"!":[...]}
  // - List membership: {"in":[{"var":"status"}, ["a", "b", "c"]]}
  // - Arithmetic: {"+":[{"var":"a"}, {"var":"b"}]}, {"*":[...]}
  // - String concat: {"cat":["Hello, ", {"var":"name"}]}
  // - Truthiness: {"!!":[{"var":"value"}]} (non-empty string is true)
  // end_jsonlogic_syntax
}
