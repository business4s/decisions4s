package decisions4s.example.docs

//start_imports
import decisions4s.*
//end_imports

object PullRequestDecision {

  // start_shapes
  case class Input[F[_]](
      numOfApprovals: F[Int],
      isTargetBranchProtected: F[Boolean],
      authorIsAdmin: F[Boolean],
  ) derives HKD

  case class Output[F[_]](
      allowMerging: F[Boolean],
      notifyUnusualAction: F[Boolean],
  ) derives HKD
  // end_shapes

  // start_table
  val decisionTable: DecisionTable[Input, Output, HitPolicy.First] =
    DecisionTable(rules, name = "PullRequestDecision", HitPolicy.First)
  // end_table

  // start_rules
  def rules: List[Rule[Input, Output]] = List(
    // Unprotected branch requires 1 approval
    Rule(
      matching = Input(
        numOfApprovals = it > 0,
        isTargetBranchProtected = it.isFalse,
        authorIsAdmin = it.catchAll,
      ),
      output = Output(allowMerging = true, notifyUnusualAction = false),
    ),
    // Protected branch requires 2 approvals
    Rule(
      matching = Input(
        numOfApprovals = it > 1,
        isTargetBranchProtected = it.isTrue,
        authorIsAdmin = it.catchAll,
      ),
      output = Output(allowMerging = true, notifyUnusualAction = false),
    ),
    // Admin can merge anything without approvals but this sends a notification
    Rule(
      matching = Input(
        numOfApprovals = it.catchAll,
        isTargetBranchProtected = it.catchAll,
        authorIsAdmin = it.isTrue,
      ),
      output = Output(allowMerging = true, notifyUnusualAction = true),
    ),
    // Nothing can be merged otherwise
    Rule.default(
      Output(allowMerging = false, notifyUnusualAction = false),
    ),
  )
  // end_rules

  def main(args: Array[String]): Unit = {

    // start_evaluate
    val result = decisionTable.evaluateFirst(
      Input[Value](
        numOfApprovals = 1,
        isTargetBranchProtected = false,
        authorIsAdmin = true,
      ),
    )
    assert(result.output == Some(Output[Value](allowMerging = true, notifyUnusualAction = false)))
    // end_evaluate

    println(result.makeDiagnosticsString)


    // start_dmn
    import decisions4s.dmn.DmnConverter
    val dmnInstance = DmnConverter.convert(decisionTable)
    import org.camunda.bpm.model.dmn.Dmn
    Dmn.writeModelToFile(new java.io.File(s"./${decisionTable.name}.dmn"), dmnInstance)
    // end_dmn

  }

}
