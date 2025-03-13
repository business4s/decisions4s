package decisions4s.internal

import decisions4s.*
import shapeless3.deriving.Const

class DiagnosticsPrinter[Input[_[_]], Output[_[_]], Out](r: EvalResult[Input, Output, Out]) {
  import r.table.given
  import r.{input, results, table}

  private val inputNames: IndexedSeq[String] = summon[HKD[Input]].fieldNames

  def print: String = {
    s"""Evaluation diagnostics for "${table.name}"
       |Hit policy: ${table.hitPolicy}
       |Result: ${r.output}
       |Input:
       |${renderInput().indent_(2)}
       |${results.zipWithIndex.map(renderRule).mkString("\n")}""".stripMargin
  }

  private def renderInput() = {
    val inputValues: Vector[Option[Any]] =
      HKDUtils.collectFields(input.mapK[Const[Option[Any]]]([t] => (value: Option[t]) => (value: Option[Any])))
    inputValues.zipWithIndex
      .map((value, idx) => s"${inputNames(idx)}: ${value.getOrElse("<not evaluated>")}")
      .mkString("\n")
  }

  private def renderRule(rr: RuleResult[Input, Output], idx: Int): String = {
    val fieldSatisfaction: Vector[Boolean] = HKDUtils.collectFields(rr.details)
    val sign                               = if (rr.evaluationResult.isDefined) then "✓" else "✗"
    val output                             = rr.evaluationResult.map(x => s"== ${renderOutput(x)}").getOrElse("== ✗")
    s"""Rule $idx [$sign]:
       |${fieldSatisfaction.zipWithIndex.map((satisfied, fIdx) => renderRuleField(idx, fIdx, satisfied)).mkString("\n").indent_(2)}
       |  $output""".stripMargin
  }

  private val maxInputNameLen                                                     = inputNames.map(_.length).max
  private val matchingExpressions: Vector[Vector[String]]                         = table.rules.toVector.map(rule => {
    given EvaluationContext[Input] = EvaluationContext.stub
    HKDUtils.collectFields(rule.matching.mapK[Const[String]]([t] => expr => expr.renderExpression))
  })
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
  def renderOutput(output: Output[Value]): String = {
    val outputNames: IndexedSeq[String] = summon[HKD[Output]].fieldNames
    val outputValues: Vector[String]    = HKDUtils.collectFields(output.mapK[Const[String]]([t] => (value: t) => value.toString))
    val fields                          = outputValues.zipWithIndex
      .map((value, idx) => s"${outputNames(idx)} = $value")
      .mkString(", ")
    s"${output.getClass.getSimpleName}($fields)"
  }

}
