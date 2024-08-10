package decisions4s.example.checks

import decisions4s.dmn.DmnRenderer
import org.camunda.bpm.model.dmn.Dmn
import org.scalatest.freespec.AnyFreeSpec

import java.io.File

class ElderlyScamCheckDecisionTest extends AnyFreeSpec {

  "render dmn" in {
    val dmnInstance = DmnRenderer.render(ElderlyScamCheckDecision.decisionTable)
    Dmn.writeModelToFile(new File("./ElderlyScamCheckDecision.dmn"), dmnInstance)
  }

}
