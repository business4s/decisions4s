package decisions4s.internal

import decisions4s.*
import decisions4s.internal.DiagnosticsData.InputFieldIdx

class DiagnosticsPrinter(data: DiagnosticsData) {
  def print: String = {
    s"""Evaluation diagnostics for "${data.table.name}"
       |Hit policy: ${data.table.hitPolicy}
       |Result: ${data.output.rawValue}
       |Input:
       |${renderInput().indent_(2)}
       |${data.table.rules.map(renderRule).mkString("\n")}""".stripMargin
  }

  private def renderInput() = {
    data.input.fieldValues.toSeq
      .sortBy(_._1)
      .map((idx, value) => s"${data.input.fieldNames(idx)}: ${value.getOrElse("<not evaluated>")}")
      .mkString("\n")
  }

  private def renderRule(rr: DiagnosticsData.Rule): String = {
    val sign       = if (rr.output.isDefined) then "✓" else "✗"
    val output     = rr.output.map(x => s"== ${renderOutput(x)}").getOrElse("== ✗")
    val conditions = rr.evaluationResults.toSeq
      .sortBy(_._1)
      .map((fIdx, satisfied) => renderRuleField(fIdx, satisfied, rr))
    s"""Rule ${rr.idx} [$sign]:
       |${conditions.mkString("\n").indent_(2)}
       |  $output""".stripMargin
  }

  private val maxInputNameLen                                                                             = data.input.fieldNames.values.map(_.length).max
  private def renderRuleField(idx: InputFieldIdx, satisfied: Boolean, rule: DiagnosticsData.Rule): String = {
    val sign = if (satisfied) then "✓" else "✗"
    s"""${data.input.fieldNames(idx).padTo(maxInputNameLen, ' ')} [$sign]: ${rule.renderedConditions(idx)}""".stripMargin
  }

  extension (string: String) {
    private def indent_(spaces: Int): String = {
      val indent = " " * spaces
      string.linesIterator.map(line => indent + line).mkString("\n")
    }
  }
  def renderOutput(output: DiagnosticsData.Rule.Output): String = {
    val fields = output.fieldValues.toSeq
      .sortBy(_._1)
      .map((idx, value) => s"${data.output.fieldNames(idx)} = $value")
      .mkString(", ")
    s"${output.getClass.getSimpleName}($fields)"
  }

}
