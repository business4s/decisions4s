package decisions4s.exprs

import decisions4s.Expr
import org.camunda.feel.FeelEngine
import org.camunda.feel.api.{FailedEvaluationResult, FeelEngineApi, FeelEngineBuilder, SuccessfulEvaluationResult}

object TestUtils {

  private val engine: FeelEngineApi = new FeelEngineApi(new FeelEngine())

  def checkExpression[T](expr: Expr[Any, T], expectedEvalResult: T, expectedParseResult: Option[Any] = None): Unit = {
    import munit.Assertions.*
    assert(clue(expr.evaluate(())) == clue(expectedEvalResult))

    val feelExpression  = clue(expr.renderFeelExpression)
    val feelResult: Any = engine.evaluateExpression(feelExpression) match {
      case SuccessfulEvaluationResult(result, suppressedFailures) => result
      case FailedEvaluationResult(failure, suppressedFailures)    =>
        fail(s"Feel evaluation failed: ${failure}")
    }
    assert({
      clue(feelExpression)
      clue(feelResult) == clue(expectedParseResult.getOrElse(expectedEvalResult))
    })
  }

}
