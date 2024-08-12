package decisions4s.example.provider_routing

import decisions4s.*
import decisions4s.dmn.DmnRenderer
import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{Files, Path}

class BankingProviderDecisionTest extends AnyFreeSpec {
  import decisions4s.example.provider_routing.BankingProviderDecision.Input

  "evaluate" - {

    "from eea to foo inc" in {
      val result = BankingProviderDecision.decisionTable
        .evaluateFirst(
          Input[Value](
            userResidenceCountry = IsEEA.eeaCountries.head,
            currency = Currency("PLN"),
          ),
        )
        .output
      assert(result.get.provider == Provider.FooInc)
    }
    "from pln to foo inc" in {
      val result = BankingProviderDecision.decisionTable
        .evaluateFirst(
          Input[Value](
            userResidenceCountry = Country("XXX"),
            currency = Currency.PLN,
          ),
        )
        .output
      assert(result.get.provider == Provider.BarLtd)
    }

  }

  "render dmn" in {
    val dmnInstance = DmnRenderer.render(BankingProviderDecision.decisionTable)
    Files.writeString(Path.of("./banking-provider-decision.dmn"), dmnInstance.toXML)
  }

}
