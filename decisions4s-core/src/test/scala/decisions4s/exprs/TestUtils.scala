package decisions4s.exprs

import decisions4s.Expr
import org.camunda.feel.FeelEngine
import org.camunda.feel.api.{FailedEvaluationResult, FeelEngineApi, SuccessfulEvaluationResult}
import org.scalatest.Assertions

object TestUtils extends Assertions {

  val engine: FeelEngineApi = new FeelEngineApi(new FeelEngine())

  def checkUnaryExpression[I](expr: UnaryTest[I], input: I, expectedEvalResult: Boolean, expectedParseResult: Option[Any] = None): Unit = {

    assert(expr.evaluate(input) == expectedEvalResult)

    val feelExpression = expr.renderExpression
    withClue(feelExpression) {
      val feelResult: Any = engine.evaluateUnaryTests(feelExpression, input) match {
        case SuccessfulEvaluationResult(result, suppressedFailures) => result
        case FailedEvaluationResult(failure, suppressedFailures)    =>
          fail(s"Feel evaluation failed: ${failure}")
      }
      assert(feelResult == expectedParseResult.getOrElse(expectedEvalResult))
    }
    ()
  }

  def checkExpression[T](
      expr: Expr[T],
      expectedEvalResult: T,
      expectedParseResult: Option[Any] = None,
      expectedFeelExpr: String = null,
  ): Unit = {
    assert(expr.evaluate == expectedEvalResult)

    val feelExpression = expr.renderExpression
    withClue(feelExpression) {
      Option(expectedFeelExpr).foreach(expectedFeelExpr => {
        assert(feelExpression == expectedFeelExpr)
      })

      val feelResult: Any = engine.evaluateExpression(feelExpression) match {
        case SuccessfulEvaluationResult(result, suppressedFailures) => result
        case FailedEvaluationResult(failure, suppressedFailures)    =>
          fail(s"Feel evaluation failed: ${failure}")
      }
      assert(feelResult == expectedParseResult.getOrElse(expectedEvalResult))
    }
    ()
  }

}
