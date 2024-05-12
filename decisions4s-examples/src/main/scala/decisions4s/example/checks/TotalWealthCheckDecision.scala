package decisions4s.example.checks

import decisions4s.exprs.GreaterThan
import decisions4s.syntax.*
import decisions4s.util.{FunctorK, PureK, SemigroupalK}
import decisions4s.{DecisionTable, it, LiteralShow, Name, Rule}

object TotalWealthCheckDecision {
  sealed trait TotalWealth
  object TotalWealth {
    case object `0To50k`    extends TotalWealth
    case object `50kTo200k` extends TotalWealth
    case object `200kTo1m`  extends TotalWealth
    case object `1mTo5m`    extends TotalWealth
    case object `5mOrMore`  extends TotalWealth

    def values: List[TotalWealth]  = List(`0To50k`, `50kTo200k`, `200kTo1m`, `1mTo5m`, `5mOrMore`)
    given LiteralShow[TotalWealth] = _.toString
  }

  sealed trait RiskLevel
  object RiskLevel {
    case object Low    extends RiskLevel
    case object Medium extends RiskLevel
    case object High   extends RiskLevel
    def values: List[RiskLevel]  = List(Low, Medium, High)
    given LiteralShow[RiskLevel] = _.toString
  }

  case class Input[F[_]](totalWealthDeclaration: F[TotalWealth], userRiskLevel: F[RiskLevel], sumOfDepositsEur: F[Int])
      derives FunctorK,
        SemigroupalK,
        PureK

  case class Output[F[_]](stop: F[Boolean]) derives FunctorK, SemigroupalK

  val decisionTable: DecisionTable[Input, Output] =
    DecisionTable(
      rules,
      inputNames = Name.auto[Input],
      outputNames = Name.auto[Output],
      name = "TotalWealthCheck",
    )

  private type Rule = decisions4s.Rule[Input, Output]
  private lazy val rules: List[Rule] = for {
    answer <- TotalWealth.values
    rules  <- calculateThreshold(answer) match {
                case Some(getThreshold) =>
                  for {
                    riskLevel <- RiskLevel.values
                  } yield Rule(
                    matching = Input(
                      totalWealthDeclaration = it === answer,
                      userRiskLevel = it === riskLevel,
                      sumOfDepositsEur = it > getThreshold(riskLevel),
                    ),
                    output = Output(stop = true.asLiteral),
                  )
                case None               =>
                  List(
                    Rule(
                      matching = Input(
                        totalWealthDeclaration = it === answer,
                        userRiskLevel = it.catchAll,
                        sumOfDepositsEur = it.catchAll,
                      ),
                      output = Output(stop = false.asLiteral),
                    ),
                  )
              }
  } yield rules

  def calculateThreshold(answer: TotalWealth): Option[RiskLevel => Int] = answer match {
    case TotalWealth.`0To50k`    => Some(rl => (50_000 * getTolerance(rl)).toInt)
    case TotalWealth.`50kTo200k` => Some(rl => (200_000 * getTolerance(rl)).toInt)
    case TotalWealth.`200kTo1m`  => Some(rl => (1_000_000 * getTolerance(rl)).toInt)
    case TotalWealth.`1mTo5m`    => Some(rl => (5_000_000 * getTolerance(rl)).toInt)
    case TotalWealth.`5mOrMore`  => None
  }

  def getTolerance(riskLevel: RiskLevel): Double = riskLevel match {
    case RiskLevel.Low    => 2.5
    case RiskLevel.Medium => 1.25
    case RiskLevel.High   => 0.25
  }

}
