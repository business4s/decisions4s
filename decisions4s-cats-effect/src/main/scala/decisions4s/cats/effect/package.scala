package decisions4s.cats

import cats.effect.{Async, Concurrent}
import decisions4s.DecisionTable.HitPolicy
import decisions4s.{DecisionTable, EvalResult}

package object effect {

  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.First]) {
    def evaluateFirstF[F[_]: Concurrent: Async](in: Input[F]): F[EvalResult.First[Input, Output]] = {
      new MemoizingEvaluator(dt).evaluateFirst(in)
    }
  }

}
