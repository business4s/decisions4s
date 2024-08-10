package decisions4s.internal

import decisions4s.internal.HKDUtils.Const
import decisions4s.{DecisionTable, EvaluationContext, HitPolicy}

object RenderUtils {

  def prepare[In[_[_]], Out[_[_]]](table: DecisionTable[In, Out, ?]): DecisionRenderInput = {
    import table.given
    val inputNames                              = HKDUtils.collectFields(table.inputHKD.meta.mapK[Const[String]]([t] => meta => meta.name))
    val outputNames                             = HKDUtils.collectFields(table.outputHKD.meta.mapK[Const[String]]([t] => meta => meta.name))
    given EvaluationContext[In]                 = EvaluationContext.stub
    val rules: Vector[DecisionRenderInput.Rule] = table.rules.toVector.map(rule => {
      val inputs  = HKDUtils.collectFields(rule.matching.mapK([t] => test => test.renderExpression: Const[String][t]))
      val outputs = HKDUtils.collectFields(rule.output.mapK([t] => expr => expr.renderExpression: Const[String][t]))
      DecisionRenderInput.Rule(inputs, outputs, rule.annotation)
    })
    DecisionRenderInput(table.name, inputNames, outputNames, rules, table.hitPolicy)
  }

  case class DecisionRenderInput(
      name: String,
      inputNames: Vector[String],
      outputNames: Vector[String],
      rules: Vector[DecisionRenderInput.Rule],
      hitPolicy: HitPolicy,
  )

  object DecisionRenderInput {
    case class Rule(inputs: Vector[String], outputs: Vector[String], annotation: Option[String])
  }

}
