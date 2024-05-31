package decisions4s.cats

import cats.effect.{Concurrent}
import decisions4s.{DecisionTable, HKD}
import decisions4s.DecisionTable.HitPolicy
import decisions4s.internal.FirstEvalResult

package object effect {

  extension [Input[_[_]]: HKD, Output[_[_]]: HKD](dt: DecisionTable[Input, Output, HitPolicy.First]) {
    def evaluateFirstF[F[_]: Concurrent](in: Input[F]): F[FirstEvalResult[Input, Output]] = {
      new MemoizingEvaluator(dt).evaluateFirst(in)
    }
  }



}
