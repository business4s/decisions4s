package decisions4s.example.provider_routing

import decisions4s.*
import decisions4s.syntax.*
import decisions4s.util.{FunctorK, PureK, SemigroupalK}

object BankingProviderDecision {

  case class Input[F[_]](userResidenceCountry: F[Country], currency: F[Currency]) derives FunctorK, SemigroupalK, PureK

  case class Output[F[_]](provider: F[Provider]) derives FunctorK, SemigroupalK

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
        currency = catchAll,
      ),
      output = Output(
        provider = Provider.FooInc.asLiteral,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = catchAll,
        currency = Currency.EUR.matchEqual,
      ),
      output = Output(
        provider = Provider.AcmeCorp.asLiteral,
      ),
    ),
    Rule(
      matching = Input(
        userResidenceCountry = catchAll,
        currency = List(Currency.CHF, Currency.PLN).matchAny,
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
