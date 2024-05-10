package decisions4s

import cats.data.Tuple2K
import decisions4s.internal.HKDUtils
import decisions4s.syntax.catchAll
import decisions4s.util.FunctorK.syntax.mapK
import decisions4s.util.SemigroupalK.syntax.productK
import decisions4s.util.{FunctorK, PureK, SemigroupalK}
import shapeless3.deriving.~>

case class Rule[Input[_[_]]: FunctorK: SemigroupalK, Output[_[_]]: FunctorK](
    matching: Input[MatchingExpr],
    output: Output[ValueExpr],
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] = {
    type Bool[T] = Boolean
    type Tup[T]  = Tuple2K[MatchingExpr, Value, T]
    val evaluateMatch: Tup ~> Bool   = [t] => (tuple: Tup[t]) => tuple.first.evaluate(tuple.second)
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
  def default[Input[_[_]]: FunctorK: SemigroupalK: PureK, Output[_[_]]: FunctorK](value: Output[ValueExpr]): Rule[Input, Output] = {
    Rule(
      matching = PureK[Input].pure[MatchingExpr]([t] => () => catchAll[t]),
      output = value,
    )
  }
}
