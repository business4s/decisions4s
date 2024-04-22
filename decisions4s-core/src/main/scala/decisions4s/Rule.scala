package decisions4s

import cats.arrow.FunctionK
import cats.data.Tuple2K
import cats.tagless.{FunctorK, SemigroupalK}
import cats.tagless.syntax.all.*
import cats.{SemigroupK, ~>}
import decisions4s.exprs.Literal

case class Rule[Input[_[_]]: FunctorK: SemigroupalK, Output[_[_]]: FunctorK](
    matching: Input[MatchingExpr],
    output: Output[ValueExpr],
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] = {
    var matches = true
    type Void[T] = Any
    type Tup[T]  = Tuple2K[MatchingExpr, Value, T]
    val checkMatching: Tup ~> Void   = new FunctionK[Tup, Void] {
      override def apply[A](fa: Tup[A]): Void[A] = {
        val fieldMatches = fa.first(Literal(fa.second)).evaluate
        matches = matches && fieldMatches
      }
    }
    val _                            = matching.productK(in).mapK(checkMatching)
    val evaluate: ValueExpr ~> Value = new FunctionK[ValueExpr, Value] {
      override def apply[A](fa: ValueExpr[A]): Value[A] = fa.evaluate
    }
    Option.when(matches)(output.mapK(evaluate))
  }

  def render(): (Input[Description], Output[Description]) = ???

}
