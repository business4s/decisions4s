package decisions4s.cats.effect

import _root_.cats.effect.IO
import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec
class MemoizedEvalTest extends AnyFreeSpec {

  case class Input[F[_]](a: F[Int], b: F[Int], c: F[Int]) derives HKD
  case class Output[F[_]](d: F[Int]) derives HKD

  val testTable: DecisionTable[Input, Output, HitPolicy.First] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(it > 0, it.catchAll, it.catchAll),
        output = Output(1),
      ),
      Rule(
        matching = Input(it < -10, it > 0, it.catchAll),
        output = Output(2),
      ),
      Rule(
        matching = Input(it.catchAll, it.catchAll, it > 0),
        output = Output(3),
      ),
    ),
    "test",
    HitPolicy.First,
  )

  "first rule triggered" in {
    val (result, counters) = evaluate(Input(1, 0, 0))
    assert(result.output.map(_.d).contains(1))
    assert(counters == Input[Counter](1, 0, 0))
  }

  "second rule triggered" in {
    val (result, counters) = evaluate(Input(-11, 1, 0))
    assert(result.output.map(_.d).contains(2))
    assert(counters == Input[Counter](1, 1, 0))
  }

  "third rule triggered" in {
    val (result, counters) = evaluate(Input(-1, 0, 1))
    assert(result.output.map(_.d).contains(3))
    assert(counters == Input[Counter](1, 1, 1))
  }

  "wholeInput" in {
    val testTable: DecisionTable[Input, Output, HitPolicy.First] = DecisionTable(
      rules = List(
        Rule(
          matching = Input(
            a = it.catchAll,
            b = it.catchAll,
            c = wholeInput.b > 1,
          ),
          output = Output(wholeInput.c),
        ),
      ),
      "test",
      HitPolicy.First,
    )
    val (result, counters)                                       = evaluate(Input(0, 2, 1), testTable)
    println(result.makeDiagnosticsString)
    assert(result.output.map(_.d).contains(1))
    assert(counters == Input[Counter](0, 1, 1))
  }

  def evaluate(
      input: Input[Value],
      table: DecisionTable[Input, Output, HitPolicy.First] = testTable,
  ): (EvalResult.First[Input, Output], Input[Counter]) = {
    import _root_.cats.effect.unsafe.implicits.given
    val (effectfulInput, getCounters) = createCountingInput(input)
    val result                        = table.evaluateFirstF(effectfulInput).unsafeRunSync()
    (result, getCounters())
  }

  type Counter[T] = Int
  // creates input that counts how many times an IO was evaluated.
  def createCountingInput(values: Input[Value]): (Input[IO], () => Input[Counter]) = {
    var (a, b, c)      = (0, 0, 0)
    val getCounters    = () => Input[Counter](a, b, c)
    val effectfulInput = Input[IO](
      IO({ a += 1; values.a }),
      IO({ b += 1; values.b }),
      IO({ c += 1; values.c }),
    )
    (effectfulInput, getCounters)
  }

}
