package decisions4s.html

import decisions4s.DecisionTable
import decisions4s.internal.RenderUtils
import decisions4s.internal.RenderUtils.DecisionRenderInput

object HtmlRenderer {

  def render[In[_[_]], Out[_[_]]](table: DecisionTable[In, Out, ?]): String = {
    val prepared = RenderUtils.prepare(table)
    renderPrepared(prepared)
  }

  private def renderPrepared(data: DecisionRenderInput): String = {
    val inputCols  = data.inputNames.size
    val outputCols = data.outputNames.size
    val annoCols   = if (data.rules.exists(_.annotation.isDefined)) 1 else 0
    val totalCols  = inputCols + outputCols + annoCols

    s"""<!DOCTYPE html>
       |<html>
       |<head>
       |<style>
       |  .decision-table {
       |    font-family: Arial, sans-serif;
       |    border-collapse: collapse;
       |    width: auto;
       |    margin: 20px 0;
       |  }
       |  .decision-table th, .decision-table td {
       |    border: 1px solid #ddd;
       |    padding: 8px;
       |    text-align: left;
       |  }
       |  .decision-table th {
       |    background-color: #f2f2f2;
       |  }
       |  .header-main {
       |    text-align: center;
       |    font-weight: bold;
       |  }
       |  .section-header {
       |    background-color: #e0e0e0;
       |    text-align: center;
       |    font-weight: bold;
       |  }
       |</style>
       |</head>
       |<body>
       |
       |<table class="decision-table">
       |  <thead>
       |    <tr>
       |      <th colspan="$totalCols" class="header-main">${data.name} (Hit Policy: ${data.hitPolicy})</th>
       |    </tr>
       |    <tr>
       |      <th colspan="$inputCols" class="section-header">Inputs</th>
       |      <th colspan="$outputCols" class="section-header">Outputs</th>
       |      ${if (annoCols > 0) "<th class=\"section-header\">Annotations</th>" else ""}
       |    </tr>
       |    <tr>
       |      ${data.inputNames.map(n => s"<th>$n</th>").mkString("\n      ")}
       |      ${data.outputNames.map(n => s"<th>$n</th>").mkString("\n      ")}
       |      ${if (annoCols > 0) "<th>Annotation</th>" else ""}
       |    </tr>
       |  </thead>
       |  <tbody>
       |    ${data.rules.map(renderRule(annoCols > 0)).mkString("\n    ")}
       |  </tbody>
       |</table>
       |
       |</body>
       |</html>
       |""".stripMargin
  }

  private def renderRule(hasAnnotations: Boolean)(rule: DecisionRenderInput.Rule): String = {
    val inputs  = rule.inputs.map(v => s"<td>${escapeHtml(v)}</td>").mkString
    val outputs = rule.outputs.map(v => s"<td>${escapeHtml(v)}</td>").mkString
    val anno    = if (hasAnnotations) s"<td>${escapeHtml(rule.annotation.getOrElse(""))}</td>" else ""
    s"<tr>$inputs$outputs$anno</tr>"
  }

  private def escapeHtml(s: String): String =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
}
