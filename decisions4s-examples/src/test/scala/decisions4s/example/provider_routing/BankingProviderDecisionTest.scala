package decisions4s.example.provider_routing

import decisions4s.*
import decisions4s.dmn.DmnConverter
import org.camunda.bpm.model.dmn.Dmn
import org.scalatest.freespec.AnyFreeSpec

import java.io.File
import scala.annotation.experimental

@experimental
class BankingProviderDecisionTest extends AnyFreeSpec {
  import decisions4s.example.provider_routing.BankingProviderDecision.Input

  "evaluate" - {

    "from eea to foo inc" in {
      val result = BankingProviderDecision.decisionTable.evaluate(
        Input[Value](
          userResidenceCountry = IsEEA.eeaCountries.head,
          currency = Currency("PL"),
        ),
      )

      assert(result.get.provider == Provider.FooInc)
    }

  }

  "render dmn" in {
    val dmnInstance = DmnConverter.convert(BankingProviderDecision.decisionTable)
    Dmn.writeModelToFile(new File("./banking-provider-decision.dmn"), dmnInstance)
  }

}
