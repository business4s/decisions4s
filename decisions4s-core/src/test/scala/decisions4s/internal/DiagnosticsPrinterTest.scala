package decisions4s.internal

import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec

class DiagnosticsPrinterTest extends AnyFreeSpec {

  case class Input[F[_]](inputField1: F[String], inputField2Something: F[Int]) derives HKD
  case class Output[F[_]](outputField1: F[Double], outputField2: F[Boolean]) derives HKD

  val table = DecisionTable[Input, Output, HitPolicy.Single](
    List(
      Rule(
        Input(inputField1 = it.equalsTo("foo"), inputField2Something = it.catchAll),
        Output(outputField1 = 1.0, outputField2 = true),
        annotation = Some("foo"),
      ),
      Rule(
        Input(inputField1 = it.equalsTo("bar"), inputField2Something = it > 1),
        Output(outputField1 = 2.0, outputField2 = false),
        annotation = Some("bar"),
      ),
    ),
    "test",
    HitPolicy.Single,
  )

  "basic" in {
    val result = table.evaluateSingle(Input("foo", 1))
    assert(
      result.makeDiagnosticsString ==
        """Evaluation diagnostics for "test"
          |Hit policy: Single
          |Result: Right(Some(Output(1.0,true)))
          |Input:
          |  inputField1: foo
          |  inputField2Something: 1
          |Rule 0 [✓]: foo
          |  inputField1          [✓]: "foo"
          |  inputField2Something [✓]: -
          |  == Output(outputField1 = 1.0, outputField2 = true)
          |Rule 1 [✗]: bar
          |  inputField1          [✗]: "bar"
          |  inputField2Something [✗]: > 1
          |  == ✗""".stripMargin,
    )
  }
}
