package decisions4s.example.checks

import decisions4s.exprs.{GreaterThan, LessThan}
import decisions4s.syntax.*
import decisions4s.util.{FunctorK, PureK, SemigroupalK}
import decisions4s.{DecisionTable, Name, Rule}

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
        accountAgeDays = LessThan(30.asLiteral),
        userAgeYears = GreaterThan(55.asLiteral),
        cryptoWithdrawalsCount = LessThan(3.asLiteral),
        txAmountEur = GreaterThan(2000.asLiteral),
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
