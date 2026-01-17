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
        Map("price"    -> "price > 100", "quantity" -> "quantity >= 10"),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map("price"    -> "true", "quantity" -> "true"),
        Map("discount" -> "0.0"),
        Some("No discount"),
      ),
    ),
    "pricing",
  )

  val celTable = CelDecisionTable
    .load(
      celDto,
      HKD.gatherGivens[PricingInput, ToCelType], // required to declare input variables
      HKD.gatherGivens[PricingOutput, FromCel], // required to extract output variables
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
        Map("price"    -> "> 100", "quantity" -> ">= 10"),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map("price"    -> "> 0", "quantity" -> "> 0"),
        Map("discount" -> "0.0"),
        Some("No discount"),
      ),
    ),
    "pricing",
  )

  val feelTable = FeelDecisionTable
    .load[PricingInput, PricingOutput, HitPolicy.First](
      feelDto,
      HKD.gatherGivens[PricingOutput, FromFeel], // required to extract output variables
      HitPolicy.First,
    )
    .get

  val feelResult = feelTable.evaluateFirst(PricingInput(150, 20))
  // feelResult.output == Some(PricingOutput[Value](0.1))
  // end_feel

  // start_jsonlogic
  import decisions4s.persistence.jsonlogic.*

  val jsonLogicDto = DecisionTableDTO(
    Seq(
      DecisionTableDTO.Rule(
        // json-logic uses JSON objects for expressions
        Map(
          "price"      -> """{">":[{"var":"price"}, 100]}""",
          "quantity"   -> """{">=":[{"var":"quantity"}, 10]}""",
        ),
        Map("discount" -> "0.1"),
        Some("Bulk discount"),
      ),
      DecisionTableDTO.Rule(
        Map(
          "price"      -> """{">":[{"var":"price"}, 0]}""",
          "quantity"   -> """{">":[{"var":"quantity"}, 0]}""",
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
      HKD.gatherGivens[PricingOutput, FromJsonLogic], // required to extract output variables
      HitPolicy.First,
    )
    .get

  val jsonLogicResult = jsonLogicTable.evaluateFirst(PricingInput(150, 20))
  // jsonLogicResult.output == Some(PricingOutput[Value](0.1))
  // end_jsonlogic
}
