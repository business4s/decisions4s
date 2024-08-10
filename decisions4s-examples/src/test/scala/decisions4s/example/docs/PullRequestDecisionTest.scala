package decisions4s.example.docs

import decisions4s.markdown.MarkdownRenderer
import org.scalatest.freespec.AnyFreeSpec

import scala.io.Source

class PullRequestDecisionTest extends AnyFreeSpec {

  "diagnostics" in {
    val liveString = PullRequestDecision.result.makeDiagnosticsString

    val expected = Source.fromURL(getClass.getResource("/docs/pull-request-diagnostics.txt")).mkString

    assert(liveString == expected)
  }

  "markdown" in {
    val liveString = MarkdownRenderer.render(PullRequestDecision.decisionTable)

    val expected = Source.fromURL(getClass.getResource("/docs/pull-request-markdown.md")).mkString

    assert(liveString == expected)
  }

}
