package decisions4s.markdown

import decisions4s.DecisionTable
import decisions4s.internal.RenderUtils

object MarkdownRenderer {

  def render[In[_[_]], Out[_[_]]](table: DecisionTable[In, Out, ?]): String = {
    val prepared                    = RenderUtils.prepare(table)
    val atLEastOneAnnotationDefined = prepared.rules.exists(_.annotation.isDefined)
    val mdTable                     = MarkdownTable(
      headers = prepared.inputNames.map(x => s"(I) ${x}") ++
        prepared.outputNames.map(x => s"(O) ${x}") ++
        (if (atLEastOneAnnotationDefined) Vector("Annotations") else Vector()),
      values = prepared.rules.map(r => r.inputs ++ r.outputs ++ r.annotation),
    )
    mdTable.render
  }

}
