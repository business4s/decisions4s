package decisions4s.example.checks

import decisions4s.dmn.DmnRenderer
import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{Files, Path}

class ElderlyScamCheckDecisionTest extends AnyFreeSpec {

  "render dmn" in {
    val dmnInstance = DmnRenderer.render(ElderlyScamCheckDecision.decisionTable)
    Files.writeString(Path.of("./ElderlyScamCheckDecision.dmn"), dmnInstance.toXML)
  }

}
