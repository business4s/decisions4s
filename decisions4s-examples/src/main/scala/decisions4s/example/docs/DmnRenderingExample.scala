package decisions4s.example.docs

object DmnRenderingExample {

  import decisions4s.HKD
  case class Input[F[_]]() derives HKD
  case class Output[F[_]]() derives HKD

  // start_dmn_raw
  import decisions4s.*
  import decisions4s.dmn.*
  import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance}

  val decisionTable: DecisionTable[Input, Output, ?] = ???
  val dmn: DmnModelInstance                          = DmnConverter.convert(decisionTable)
  val dmnXml: String                                 = Dmn.convertToString(dmn)
  // end_dmn_raw

  // start_dmn_image
  import decisions4s.dmn.image.*

  val converter = DmnToImageConverter()
  converter.convertDiagram(dmnXml)
  // end_dmn_image

}
