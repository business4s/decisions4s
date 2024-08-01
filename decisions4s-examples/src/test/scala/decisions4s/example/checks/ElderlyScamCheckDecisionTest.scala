package decisions4s.example.checks

import decisions4s.dmn.DmnConverter
import org.camunda.bpm.model.dmn.Dmn
import org.scalatest.freespec.AnyFreeSpec

import java.io.File

class ElderlyScamCheckDecisionTest extends AnyFreeSpec {

  "render dmn" in {
    val dmnInstance = DmnConverter.convert(ElderlyScamCheckDecision.decisionTable)
    Dmn.writeModelToFile(new File("./ElderlyScamCheckDecision.dmn"), dmnInstance)
  }

}
