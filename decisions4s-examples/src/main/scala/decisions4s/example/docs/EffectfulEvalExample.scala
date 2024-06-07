package decisions4s.example.docs

import decisions4s.HKD

object EffectfulEvalExample {

  case class Input[F[_]]() derives HKD
  case class Output[F[_]]() derives HKD

  // start_effect
  import cats.effect.IO
  import decisions4s.*
  import decisions4s.cats.effect.*

  val decisionTable: DecisionTable[Input, Output, HitPolicy.First] = ???
  val input: Input[IO] = ???

  decisionTable.evaluateFirstF(input).map(_.output)
  // end_effect
}
