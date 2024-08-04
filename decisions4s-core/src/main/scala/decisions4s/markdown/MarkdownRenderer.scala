package decisions4s.markdown

import decisions4s.internal.HKDUtils
import decisions4s.internal.HKDUtils.Const
import decisions4s.{DecisionTable, EvaluationContext}

object MarkdownRenderer {

  def render[In[_[_]], Out[_[_]]](table: DecisionTable[In, Out, ?]): String = {

    import table.{inputHKD, outputHKD}

    val inputNames                = HKDUtils.collectFields(table.inputHKD.meta.mapK([t] => meta => meta.name: Const[String][t])).map(x => s"In:${x}")
    val outputNames               = HKDUtils.collectFields(table.outputHKD.meta.mapK([t] => meta => meta.name: Const[String][t])).map(x => s"Out:${x}")
    given EvaluationContext[In]   = EvaluationContext.stub
    val rows: Seq[Vector[String]] = table.rules.map(rule => {
      val rules   = HKDUtils.collectFields(rule.matching.mapK([t] => test => test.renderExpression: Const[String][t]))
      val outputs = HKDUtils.collectFields(rule.output.mapK([t] => expr => expr.renderExpression: Const[String][t]))
      rules ++ outputs ++ Vector(rule.annotation.getOrElse(""))
    })

    val mdTable = MarkdownTable(
      headers = inputNames ++ outputNames ++ Vector("Annotations"),
      values = rows.toVector,
    )

    s"name (hit policy)"
    mdTable.render
  }

}
