package decisions4s.example.provider_routing

import decisions4s.*
import decisions4s.syntax.*
import cats.data.Tuple2K
import cats.tagless.{FunctorK, SemigroupalK}
import cats.~>

import scala.annotation.experimental

@experimental
object BankingProviderDecision {

  case class Input[F[_]](userResidenceCountry: F[Country], currency: F[Currency]) // derives FunctorK, SemigroupalK

  case class Output[F[_]](provider: F[Provider]) // derives FunctorK, SemigroupalK

  val decisionTable: DecisionTable[Input, Output] =
    DecisionTable(
      rules,
      inputNames = Name.auto[Input],
      outputNames = Name.auto[Output],
    )

  type Rule = decisions4s.Rule[Input, Output]

  lazy val rules: List[Rule] = List(
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
    Rule(
      matching = Input(
        userResidenceCountry = catchAll,
        currency = catchAll,
      ),
      output = Output(
        provider = Provider.BazCo.asLiteral,
      ),
    )
  )

  implicit lazy val inputI: FunctorK[Input] with SemigroupalK[Input] = new FunctorK[Input] with SemigroupalK[Input] {
    override def mapK[F[_], G[_]](af: Input[F])(fk: F ~> G): Input[G] = Input(fk(af.userResidenceCountry), fk(af.currency))

    override def productK[F[_], G[_]](af: Input[F], ag: Input[G]): Input[[_$5] =>> Tuple2K[F, G, _$5]] = Input(
      Tuple2K(af.userResidenceCountry, ag.userResidenceCountry),
      Tuple2K(af.currency, ag.currency),
    )
  }

  implicit lazy val outputI: FunctorK[Output] = new FunctorK[Output] {

    override def mapK[F[_], G[_]](af: Output[F])(fk: F ~> G): Output[G] = Output(fk(af.provider))
  }

}
