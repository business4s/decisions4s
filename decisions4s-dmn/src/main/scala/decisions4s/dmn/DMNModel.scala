package decisions4s.dmn

import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance}

case class DMNModel(raw: DmnModelInstance) {
  def toXML: String = Dmn.convertToString(raw)
}
