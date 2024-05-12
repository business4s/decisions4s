package decisions4s.example.checks

import decisions4s.syntax.*
import decisions4s.util.{FunctorK, PureK, SemigroupalK}
import decisions4s.{DecisionTable, it, Name, Rule}

object ElderlyScamCheckDecision {

  case class Input[F[_]](accountAgeDays: F[Int], userAgeYears: F[Int], cryptoWithdrawalsCount: F[Int], txAmountEur: F[Int])
      derives FunctorK,
        SemigroupalK,
        PureK

  case class Output[F[_]](stop: F[Boolean]) derives FunctorK, SemigroupalK

  val decisionTable: DecisionTable[Input, Output] =
    DecisionTable(
      rules,
      inputNames = Name.auto[Input],
      outputNames = Name.auto[Output],
      name = "ElderlyScamCheck"
    )

  private type Rule = decisions4s.Rule[Input, Output]
  private lazy val rules: List[Rule] = List(
    Rule(
      matching = Input(
        accountAgeDays = it < 30,
        userAgeYears = it > 55,
        cryptoWithdrawalsCount = it < 3,
        txAmountEur = it > 2000,
      ),
      output = Output(
        stop = true.asLiteral,
      ),
    ),
    Rule.default(
      Output(
        stop = false.asLiteral,
      ),
    ),
  )

}
