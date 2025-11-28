package decisions4s

case class DiagnosticsData(
    table: DiagnosticsData.Table,
    input: DiagnosticsData.Input,
    output: DiagnosticsData.Output,
)

object DiagnosticsData {

  opaque type RuleIdx <: Int = Int
  object RuleIdx        {
    def apply(value: Int): RuleIdx = value
  }
  opaque type InputFieldIdx <: Int = Int
  object InputFieldIdx  {
    def apply(value: Int): InputFieldIdx = value
  }
  opaque type OutputFieldIdx <: Int = Int
  object OutputFieldIdx {
    def apply(value: Int): OutputFieldIdx = value
  }

  case class Table(
      name: String,
      hitPolicy: HitPolicy,
      rules: Seq[Rule],
  )
  case class Rule(
      idx: RuleIdx,
      annotation: Option[String],
      renderedConditions: Map[InputFieldIdx, String],
      evaluation: Option[Rule.Evaluation],
  )
  case object Rule {
    case class Evaluation(
        evaluationResults: Map[InputFieldIdx, Boolean],
        output: Option[Rule.Output],
    )
    case class Output(
        rawValue: Any,
        fieldValues: Map[OutputFieldIdx, Any],
    )
  }
  case class Input(
      fieldNames: Map[InputFieldIdx, String],
      fieldValues: Map[InputFieldIdx, Option[Any]],
      rawValue: Any,
  )
  case class Output(
      fieldNames: Map[OutputFieldIdx, String],
      rawValue: Any,
  )

}
