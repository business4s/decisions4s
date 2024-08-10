package decisions4s.example.docs

import decisions4s.markdown.MarkdownRenderer

object RenderingExample {

  import decisions4s.HKD
  case class Input[F[_]]() derives HKD
  case class Output[F[_]]() derives HKD

  // start_markdown
  import decisions4s.*

  val decisionTable: DecisionTable[Input, Output, ?] = ???
  val markdown: String                               = MarkdownRenderer.render(decisionTable)
  // end_markdown

  // start_dmn_raw
  import decisions4s.dmn.*
  import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance}

  val dmn: DmnModelInstance = DmnRenderer.render(decisionTable)
  val dmnXml: String        = Dmn.convertToString(dmn)
  // end_dmn_raw

  // start_dmn_image
  import decisions4s.dmn.image.*

  val converter = DmnToImageConverter()
  converter.convertDiagram(dmnXml)
  // end_dmn_image

}
