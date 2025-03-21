package decisions4s.example.docs

import decisions4s.{DiagnosticsData, Value}
import decisions4s.markdown.MarkdownRenderer
import decisions4s.testing.SnapshotTest
import org.scalatest.freespec.AnyFreeSpec

class PullRequestDecisionTest extends AnyFreeSpec {

  "diagnostics" in {
    val liveString = PullRequestDecision.result.makeDiagnosticsString
    SnapshotTest.testSnapshot(liveString, "docs/pull-request-diagnostics.txt")
  }

  "markdown" in {
    val liveString = MarkdownRenderer.render(PullRequestDecision.decisionTable)
    SnapshotTest.testSnapshot(liveString, "docs/pull-request-markdown.md")
  }

  "generate custom diagnostics" in {
    val decisionTable = PullRequestDecision.decisionTable
    val input         = PullRequestDecision.Input[Value](1, false, false)

    // start_customization
    val diagData: DiagnosticsData = decisionTable.evaluateFirst(input).diagnosticsData
    val output: String            =
      s"""${diagData.table.name}
         |  produced ${diagData.output.rawValue}
         |  using ${diagData.table.rules.size} rules""".stripMargin
    // end_customization
    SnapshotTest.testSnapshot(output, "docs/pull-request-custom-diagnostics.txt")
  }

}
