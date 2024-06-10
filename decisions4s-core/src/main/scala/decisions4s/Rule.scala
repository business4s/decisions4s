package decisions4s

import decisions4s.internal.HKDUtils
import shapeless3.deriving.{Const, ~>}

case class Rule[Input[_[_]]: HKD, Output[_[_]]: HKD](
    matching: Input[MatchingExpr],
    output: Output[OutputValue],
    annotation: Option[String] = None,
) {

  def evaluateOutput(): Output[Value] = {
    val evaluate: ValueExpr ~> Value = [t] => (fa: ValueExpr[t]) => fa.evaluate(())
    output.mapK(evaluate)
  }

  def evaluate(in: Input[Value]): Rule.Result[Input, Output] = {
    type Bool[T] = Boolean
    type Tup[T]  = Tuple2K[MatchingExpr, Value][T]
    val evaluateMatch: Tup ~> Bool        = [t] => (tuple: Tup[t]) => tuple._1.evaluate(tuple._2)
    val evaluated: Input[Bool]            = matching.productK(in).mapK(evaluateMatch)
    val matches                           = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evalResult: Option[Output[Value]] = Option.when(matches)(evaluateOutput())
    Rule.Result(evaluated, evalResult)
  }

  def render(): (Input[Description], Output[Description]) = {
    val renderInput: MatchingExpr ~> Description = [t] => (expr: MatchingExpr[t]) => expr.renderExpression
    val renderOutput: ValueExpr ~> Description   = [t] => (expr: ValueExpr[t]) => expr.renderExpression
    (matching.mapK(renderInput), output.mapK(renderOutput))
  }

}

object Rule {
  def default[Input[_[_]]: HKD, Output[_[_]]: HKD](value: Output[OutputValue]): Rule[Input, Output] = {
    Rule(
      matching = HKD[Input].pure[MatchingExpr]([t] => () => it.catchAll[t]),
      output = value,
    )
  }

  case class Result[Input[_[_]], Output[_[_]]](details: Input[Const[Boolean]], evaluationResult: Option[Output[Value]])
}
