package decisions4s

import cats.arrow.FunctionK
import cats.data.Tuple2K
import cats.tagless.syntax.all.*
import cats.tagless.{FunctorK, SemigroupalK}
import cats.~>
import decisions4s.exprs.{Literal, Variable}
import decisions4s.internal.HKDUtils

case class Rule[Input[_[_]]: FunctorK: SemigroupalK, Output[_[_]]: FunctorK](
    matching: Input[MatchingExpr],
    output: Output[ValueExpr],
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] = {
    type Bool[T] = Boolean
    type Tup[T]  = Tuple2K[MatchingExpr, Value, T]
    val evaluateMatch: Tup ~> Bool   = FunctionK.lift[Tup, Bool]([t] => (tuple: Tup[t]) => tuple.first(Literal(tuple.second)).evaluate)
    val evaluated                    = matching.productK(in).mapK(evaluateMatch)
    val matches                      = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evaluate: ValueExpr ~> Value = new FunctionK[ValueExpr, Value] {
      override def apply[A](fa: ValueExpr[A]): Value[A] = fa.evaluate
    }
    Option.when(matches)(output.mapK(evaluate))
  }

  def render(): (Input[Description], Output[Description]) = {
    val renderInput: MatchingExpr ~> Description =
      FunctionK.lift[MatchingExpr, Description]([t] => (expr: MatchingExpr[t]) => expr.apply(Variable("x")).describe)
    val renderOutput: ValueExpr ~> Description   = FunctionK.lift[ValueExpr, Description]([t] => (expr: ValueExpr[t]) => expr.describe)
    (matching.mapK(renderInput), output.mapK(renderOutput))
  }

}
