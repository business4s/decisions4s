package decisions4s.exprs

import decisions4s.Expr
import org.camunda.feel.FeelEngine
import org.camunda.feel.api.{FailedEvaluationResult, FeelEngineApi, SuccessfulEvaluationResult}

object TestUtils {

  val engine: FeelEngineApi = new FeelEngineApi(new FeelEngine())

  def checkUnaryExpression[I](expr: UnaryTest[I], input: I, expectedEvalResult: Boolean, expectedParseResult: Option[Any] = None): Unit = {
    import munit.Assertions.*
    assert(clue(expr.evaluate(input)) == clue(expectedEvalResult))

    val feelExpression  = clue(expr.renderExpression)
    val feelResult: Any = engine.evaluateUnaryTests(feelExpression, input) match {
      case SuccessfulEvaluationResult(result, suppressedFailures) => result
      case FailedEvaluationResult(failure, suppressedFailures)    =>
        fail(s"Feel evaluation failed: ${failure}")
    }
    assert({
      clue(feelExpression)
      clue(feelResult) == clue(expectedParseResult.getOrElse(expectedEvalResult))
    })
  }

  def checkExpression[T](
      expr: Expr[T],
      expectedEvalResult: T,
      expectedParseResult: Option[Any] = None,
      expectedFeelExpr: String = null,
  ): Unit = {
    import munit.Assertions.*
    assert(clue(expr.evaluate) == clue(expectedEvalResult))

    val feelExpression = clue(expr.renderExpression)

    Option(expectedFeelExpr).foreach(expectedFeelExpr => {
      assertEquals(feelExpression, expectedFeelExpr)
    })

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
