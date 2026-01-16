package decisions4s.persistence.feel

import decisions4s.Expr
import org.camunda.feel.FeelEngine
import org.camunda.feel.api.{FailedEvaluationResult, FeelEngineApi, SuccessfulEvaluationResult}

class FeelExpression[T](
    source: String,
    engine: FeelEngineApi,
    reader: Any => T,
    input: Expr[Map[String, Any]],
) extends Expr[T] {

  def evaluate: T = {
    engine.evaluateExpression(source, input.evaluate) match {
      case SuccessfulEvaluationResult(result, _) => reader(result)
      case FailedEvaluationResult(failure, _)    => throw FeelEvaluationException(source, failure.message)
    }
  }

  def renderExpression: String = source
}

class FeelUnaryTestExpression(
    source: String,
    engine: FeelEngineApi,
    input: Expr[Map[String, Any]],
    inputValueName: String,
) extends Expr[Boolean] {

  def evaluate: Boolean = {
    val context    = input.evaluate
    val inputValue = context(inputValueName)
    engine.evaluateUnaryTests(source, inputValue, context) match {
      case SuccessfulEvaluationResult(result, _) => result.asInstanceOf[Boolean]
      case FailedEvaluationResult(failure, _)    =>
        throw FeelEvaluationException(source, failure.message)
    }
  }

  def renderExpression: String = source
}

case class FeelEvaluationException(expression: String, message: String)
    extends RuntimeException(s"FEEL evaluation failed for '$expression': $message")
