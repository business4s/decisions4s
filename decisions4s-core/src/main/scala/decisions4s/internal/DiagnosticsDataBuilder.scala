package decisions4s.internal

import decisions4s.*
import decisions4s.internal.DiagnosticsData.{InputFieldIdx, OutputFieldIdx, RuleIdx}
import shapeless3.deriving.Const

class DiagnosticsDataBuilder[Input[_[_]], Output[_[_]], Out](r: EvalResult[Input, Output, Out]) {
  import r.table.given
  import r.{input, results, table}

  def get: DiagnosticsData = {
    DiagnosticsData(
      table = DiagnosticsData.Table(
        name = r.table.name,
        hitPolicy = r.table.hitPolicy,
        rules = r.results.zipWithIndex.map(buildRule.tupled),
      ),
      input = DiagnosticsData.Input(
        fieldNames = summon[HKD[Input]].fieldNames.toIndexedMap(InputFieldIdx.apply),
        fieldValues = HKDUtils
          .collectFields(input.mapK[Const[Option[Any]]]([t] => (value: Option[t]) => (value: Option[Any])))
          .toIndexedMap(InputFieldIdx.apply),
        rawValue = r.input,
      ),
      output = DiagnosticsData.Output(
        fieldNames = summon[HKD[Output]].fieldNames.toIndexedMap(OutputFieldIdx.apply),
        rawValue = r.output,
      ),
    )
  }

  private def buildRule(rr: RuleResult[Input, Output], idx: Int): DiagnosticsData.Rule = {
    val rule = table.rules(idx)
    DiagnosticsData.Rule(
      idx = RuleIdx(idx),
      annotation = rule.annotation,
      renderedConditions = {
        given EvaluationContext[Input] = EvaluationContext.stub
        HKDUtils
          .collectFields(rule.matching.mapK[Const[String]]([t] => expr => expr.renderExpression))
          .toIndexedMap(InputFieldIdx(_))
      },
      evaluationResults = HKDUtils.collectFields(rr.details).toIndexedMap(InputFieldIdx.apply),
      output = rr.evaluationResult.map(result =>
        DiagnosticsData.Rule.Output(
          rawValue = result,
          fieldValues = HKDUtils.collectFields(result.mapK[Const[Any]]([t] => (value: t) => value: Any)).toIndexedMap(OutputFieldIdx.apply),
        ),
      ),
    )
  }

  extension [T](v: Seq[T]) {
    def toIndexedMap[K](createKey: Int => K): Map[K, T] = {
      v.zipWithIndex.map((elem, idx) => (createKey(idx), elem)).toMap
    }
  }

}
