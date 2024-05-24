package decisions4s

import decisions4s.internal.HKDUtils
import shapeless3.deriving.~>

case class Rule[Input[_[_]]: HKD, Output[_[_]]: HKD](
    matching: Input[MatchingExpr],
    output: Output[OutputValue],
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] = {
    type Bool[T] = Boolean
    type Tup[T] = Tuple2K[MatchingExpr, Value][T]
    val evaluateMatch: Tup ~> Bool   = [t] => (tuple: Tup[t]) => tuple._1.evaluate(tuple._2)
    val evaluated                    = matching.productK(in).mapK(evaluateMatch)
    val matches                      = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evaluate: ValueExpr ~> Value = [t] => (fa: ValueExpr[t]) => fa.evaluate(())
    Option.when(matches)(output.mapK(evaluate))
  }

  def render(): (Input[Description], Output[Description]) = {
    val renderInput: MatchingExpr ~> Description = [t] => (expr: MatchingExpr[t]) => expr.renderFeelExpression
    val renderOutput: ValueExpr ~> Description   = [t] => (expr: ValueExpr[t]) => expr.renderFeelExpression
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
}
