package decisions4s.example.docs

import decisions4s.*
import decisions4s.DecisionTable.HitPolicy

object PullRequestDecision {

  case class Input[F[_]](numOfApprovals: F[Int], isTargetBranchProtected: F[Boolean], authorIsAdmin: F[Boolean]) derives HKD

  case class Output[F[_]](allowMerging: F[Boolean], notifyUnusualAction: F[Boolean]) derives HKD

  val decisionTable: DecisionTable[Input, Output, HitPolicy.Unique] =
    DecisionTable(
      rules,
      inputNames = Name.auto,
      outputNames = Name.auto,
      name = "PullRequestDecision",
      HitPolicy.Unique,
    )

  private def rules: List[Rule[Input, Output]] = List(
    Rule(
      matching = Input(
        numOfApprovals = it > 0,
        isTargetBranchProtected = it.isFalse,
        authorIsAdmin = it.catchAll,
      ),
      output = Output(
        allowMerging = true,
        notifyUnusualAction = false,
      ),
    ),
    Rule(
      matching = Input(
        numOfApprovals = it > 1,
        isTargetBranchProtected = it.isTrue,
        authorIsAdmin = it.catchAll,
      ),
      output = Output(
        allowMerging = true,
        notifyUnusualAction = false,
      ),
    ),
    Rule(
      matching = Input(
        numOfApprovals = it.catchAll,
        isTargetBranchProtected = it.catchAll,
        authorIsAdmin = it.isTrue,
      ),
      output = Output(
        allowMerging = true,
        notifyUnusualAction = true,
      ),
    ),
    Rule.default(
      Output(
        allowMerging = false,
        notifyUnusualAction = false,
      ),
    ),
  )

  def main(args: Array[String]): Unit = {

    val result = decisionTable.evaluateUnique(
      Input[Value](
        numOfApprovals = 1,
        isTargetBranchProtected = false,
        authorIsAdmin = true,
      ),
    )
    println(result)
    println(result.diagnostics.mkString)

    import decisions4s.dmn.DmnConverter
    val dmnInstance = DmnConverter.convert(decisionTable)
    import org.camunda.bpm.model.dmn.Dmn
    Dmn.writeModelToFile(new java.io.File(s"./${decisionTable.name}.dmn"), dmnInstance)

  }

}
