package decisions4s.example.provider_routing

import decisions4s.*
import decisions4s.syntax.*

object BankingProviderDecision {

  case class Input[F[_]](userResidenceCountry: F[Country], currency: F[Currency]) derives HKD

  case class Output[F[_]](provider: F[Provider]) derives HKD

  val decisionTable: DecisionTable[Input, Output] =
    DecisionTable(
      rules,
      inputNames = Name.auto[Input],
      outputNames = Name.auto[Output],
      name = "BankingProviderSelection"
    )

  private type Rule = decisions4s.Rule[Input, Output]
  private lazy val rules: List[Rule] = List(
    Rule(
      matching = Input(
        userResidenceCountry = IsEEA,
        currency = it.catchAll,
      ),
      output = Output(
        provider = Provider.FooInc.asLiteral,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = it.catchAll,
        currency = Currency.EUR.matchEqual,
      ),
      output = Output(
        provider = Provider.AcmeCorp.asLiteral,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = it.catchAll,
        currency = it.equalsAnyOf(Currency.CHF, Currency.PLN),
      ),
      output = Output(
        provider = Provider.BarLtd.asLiteral,
      ),
    ),
    Rule.default(
      Output(
        provider = Provider.BazCo.asLiteral,
      ),
    ),
  )

}
