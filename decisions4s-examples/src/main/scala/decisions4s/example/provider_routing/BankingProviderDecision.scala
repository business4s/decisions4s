package decisions4s.example.provider_routing

import decisions4s.*

object BankingProviderDecision {

  case class Input[F[_]](userResidenceCountry: F[Country], currency: F[Currency]) derives HKD

  case class Output[F[_]](provider: F[Provider]) derives HKD

  val decisionTable: DecisionTable[Input, Output, HitPolicy.First] =
    DecisionTable(
      rules,
      name = "BankingProviderSelection",
      HitPolicy.First,
    )

  private type Rule = decisions4s.Rule[Input, Output]
  private lazy val rules: List[Rule] = List(
    Rule(
      matching = Input(
        userResidenceCountry = it.satisfies(IsEEA.apply),
        currency = it.catchAll,
      ),
      output = Output(
        provider = Provider.FooInc,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = it.catchAll,
        currency = it === Currency.EUR,
      ),
      output = Output(
        provider = Provider.AcmeCorp,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = it.catchAll,
        currency = it.equalsAnyOf(Currency.CHF, Currency.PLN),
      ),
      output = Output(
        provider = Provider.BarLtd,
      ),
    ),
    Rule.default(
      Output(
        provider = Provider.BazCo,
      ),
    ),
  )

}
