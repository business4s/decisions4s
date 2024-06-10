package decisions4s.internal

import decisions4s.{EvalResult, HKD, MatchingExpr, Rule}
import shapeless3.deriving.Const

class DiagnosticsPrinter[Input[_[_]], Output[_[_]], Out](r: EvalResult[Input, Output, Out]) {
  import r.{input, results, table}
  import r.table.given

  private val inputNames: IndexedSeq[String]              = summon[HKD[Input]].fieldNames
  private val matchingExpressions: Vector[Vector[String]] = table.rules.toVector.map(rule =>
    HKDUtils.collectFields(rule.matching.mapK[Const[String]]([t] => (expr: MatchingExpr[t]) => expr.renderExpression)).toVector,
  )
  private val inputValues: Vector[Option[Any]]            =
    HKDUtils.collectFields(input.mapK[Const[Option[Any]]]([t] => (value: Option[t]) => (value: Option[Any]))).toVector
  private val maxInputNameLen                             = inputNames.map(_.length).max

  def print: String = {
    s"""${table.name} Diagnostics
       |Hit policy: ${table.hitPolicy}
       |Result: ${r.output}
       |Input:
       |${renderInput().indent_(2)}
       |${results.zipWithIndex.map(renderRule).mkString("\n")}""".stripMargin
  }

  private def renderInput() = {
    inputValues.zipWithIndex
      .map((value, idx) => s"${inputNames(idx)}: ${value.getOrElse("<not evaluated>")}")
      .mkString("\n")
  }

  private def renderRule(rr: Rule.Result[Input, Output], idx: Int): String = {
    val fieldSatisfaction: Vector[Boolean] = HKDUtils.collectFields(rr.details).toVector
    val sign                               = if (rr.evaluationResult.isDefined) then "✓" else "✗"
    val output                             = rr.evaluationResult.map(x => s"== ${x.toString}").getOrElse("== ✗")
    s"""Rule $idx [$sign]:
       |${fieldSatisfaction.zipWithIndex.map((satisfied, fIdx) => renderRuleField(idx, fIdx, satisfied)).mkString("\n").indent_(2)}
       |  $output""".stripMargin
  }

  private def renderRuleField(ruleIdx: Int, idx: Int, satisfied: Boolean): String = {
    val sign = if (satisfied) then "✓" else "✗"
    s"""${inputNames(idx).padTo(maxInputNameLen, ' ')} [$sign]: ${matchingExpressions(ruleIdx)(idx)}""".stripMargin
  }

  extension (string: String) {
    private def indent_(spaces: Int): String = {
      val indent = " " * spaces
      string.linesIterator.map(line => indent + line).mkString("\n")
    }
  }

}
