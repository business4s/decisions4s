package decisions4s.example.checks

import decisions4s.*
import decisions4s.DecisionTable.HitPolicy

object ElderlyScamCheckDecision {

  case class Input[F[_]](accountAgeDays: F[Int], userAgeYears: F[Int], cryptoWithdrawalsCount: F[Int], txAmountEur: F[Int]) derives HKD

  case class Output[F[_]](stop: F[Boolean]) derives HKD

  val decisionTable: DecisionTable[Input, Output, HitPolicy.Unique] =
    DecisionTable(
      rules,
      inputNames = Name.auto[Input],
      outputNames = Name.auto[Output],
      name = "ElderlyScamCheck",
      HitPolicy.Unique
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
        stop = true,
      ),
    ),
    Rule.default(
      Output(
        stop = false,
      ),
    ),
  )

}
