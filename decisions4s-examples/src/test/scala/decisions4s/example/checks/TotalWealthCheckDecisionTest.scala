package decisions4s.example.checks

import decisions4s.dmn.DmnRenderer
import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{Files, Path}

class TotalWealthCheckDecisionTest extends AnyFreeSpec {

  "render dmn" in {
    val dmnInstance = DmnRenderer.render(TotalWealthCheckDecision.decisionTable)
    Files.writeString(Path.of("./TotalWealthCheckDecision.dmn"), dmnInstance.toXML)
  }

}
