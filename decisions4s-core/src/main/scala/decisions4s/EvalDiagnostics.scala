package decisions4s

import decisions4s.internal.HKDUtils
import shapeless3.deriving.Const

case class EvalDiagnostics[Input[_[_]]: HKD, Output[_[_]]](
    results: List[Rule.Result[Input, Output]],
    table: DecisionTable[Input, Output, _],
    input: Input[Value],
) {

  private val inputNames: Vector[String]                  = HKDUtils.collectFields(table.inputNames).toVector
  private val matchingExpressions: Vector[Vector[String]] = table.rules.toVector.map(rule =>
    HKDUtils.collectFields(rule.matching.mapK[Const[String]]([t] => (expr: MatchingExpr[t]) => expr.renderFeelExpression)).toVector,
  )
  private val inputValues: Vector[Any]                    = HKDUtils.collectFields(input.mapK[Const[Any]]([t] => (value: t) => (value: Any))).toVector
  private val maxInputNameLen = inputNames.map(_.length).max


  def mkString: String = {
    s"""Evaluation diagnostics for "${table.name}"
       |Hit policy: ${table.hitPolicy}
       |Input:
       |${renderInput().indent_(2)}
       |${results.zipWithIndex.map(renderRule).mkString("\n")}""".stripMargin
  }

  private def renderInput() = {
    inputValues.zipWithIndex
      .map((value, idx) => s"${inputNames(idx)}: $value")
      .mkString("\n")
  }

  private def renderRule(rr: Rule.Result[Input, Output], idx: Int): String = {
    val fieldSatisfaction: Vector[Boolean] = HKDUtils.collectFields(rr.details).toVector
    val sign = if (rr.evaluationResult.toOption.isDefined) then "✓" else "✗"
    s"""Rule $idx [$sign]:
       |${fieldSatisfaction.zipWithIndex.map((satisfied, fIdx) => renderRuleField(idx, fIdx, satisfied)).mkString("\n").indent_(2)}
       |  Output: ${rr.evaluationResult.toOption.map(_.toString).getOrElse("")}""".stripMargin
  }

  private def renderRuleField(ruleIdx: Int, idx: Int, satisfied: Boolean): String = {
    val sign = if (satisfied) then "✓" else "✗"
    s"""${inputNames(idx).padTo(maxInputNameLen, ' ')} [$sign]: ${matchingExpressions(ruleIdx)(idx)}""".stripMargin
  }

  extension (string: String) {
    def indent_(spaces: Int): String = {
      val indent = " " * spaces
      string.linesIterator.map(line => indent + line).mkString("\n")
    }
  }

}
