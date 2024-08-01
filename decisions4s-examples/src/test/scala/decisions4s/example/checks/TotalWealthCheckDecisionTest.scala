package decisions4s.example.checks

import decisions4s.dmn.DmnConverter
import org.camunda.bpm.model.dmn.Dmn
import org.scalatest.freespec.AnyFreeSpec

import java.io.File

class TotalWealthCheckDecisionTest extends AnyFreeSpec {

  "render dmn" in {
    val dmnInstance = DmnConverter.convert(TotalWealthCheckDecision.decisionTable)
    Dmn.writeModelToFile(new File("./TotalWealthCheckDecision.dmn"), dmnInstance)
  }

}
