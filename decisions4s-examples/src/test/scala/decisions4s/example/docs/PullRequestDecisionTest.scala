package decisions4s.example.docs

import org.scalatest.freespec.AnyFreeSpec

import scala.io.Source

class PullRequestDecisionTest extends AnyFreeSpec {

  "diagnostics" in {
    val liveString = PullRequestDecision.result.makeDiagnosticsString

    val expected = Source.fromURL(getClass.getResource("/docs/pull-request-diagnostics.txt")).mkString

    assert(liveString == expected)
  }

}
