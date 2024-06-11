package decisions4s.example.docs

import decisions4s.HKD

object DiagnosticsExample {

  case class Input[F[_]]() derives HKD

  case class Output[F[_]]() derives HKD

  // start_diagnose
  import decisions4s.*

  val decisionTable: DecisionTable[Input, Output, HitPolicy.Distinct] = ???
  val input: Input[Value]                                             = ???

  decisionTable.evaluateDistinct(input).makeDiagnosticsString
  // end_diagnose

}
