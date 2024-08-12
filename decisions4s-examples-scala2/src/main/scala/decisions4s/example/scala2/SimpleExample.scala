package decisions4s.example.scala2

import decisions4s._

// we want to verify that decision tables can be _evaluated_ from scala 2
// they still have to be defined in scala 3
case object SimpleExample {

  case class Input[F[_]](a: F[Int])
  case class Output[F[_]](b: F[Int])

  def main(args: Array[String]): Unit = {
    def decisionTable: DecisionTable[Input, Output, HitPolicy.First] = ???
    val result: EvalResult.First[Input, Output]                      = DecisionTable.evaluateFirst(decisionTable)(Input[Value](1))
    val diagnostics                                                  = result.makeDiagnosticsString
    println(diagnostics)
  }

}
