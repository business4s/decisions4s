package decisions4s

import decisions4s.DecisionTable.HitPolicy
import decisions4s.Rule.EvaluationResult
import decisions4s.internal.*
import munit.FunSuite

class DecisionTableTest extends FunSuite {

  case class Input[F[_]](a: F[Int]) derives HKD
  case class Output[F[_]](c: F[Int]) derives HKD

  val uniqueTestTable: DecisionTable[Input, Output, HitPolicy.Unique] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(it > 3),
        output = Output(2),
      ),
      Rule(
        matching = Input(it > 2),
        output = Output(1),
      ),
      Rule(
        matching = Input(it > 1),
        output = Output(1),
      ),
    ),
    inputNames = Name.auto[Input],
    outputNames = Name.auto[Output],
    "test",
    HitPolicy.Unique,
  )

  test("unique - single matching rule") {
    val result: UniqueEvalResult[Input, Output]   = uniqueTestTable.evaluateUnique(Input(2))
    val expected: UniqueEvalResult[Input, Output] = UniqueEvalResult.Success(diagnostics(false, false, true), Output(1))
    assertEquals(result, expected)
  }

  test("unique - no matching rules") {
    val input                                     = Input[Value](0)
    val result                                    = uniqueTestTable.evaluateUnique(input)
    val expected: UniqueEvalResult[Input, Output] = UniqueEvalResult.NoHit(diagnostics(false, false, false))
    assertEquals(result, expected)
  }

  test("unique - multiple matching rules") {
    val input                                     = Input[Value](3)
    val result                                    = uniqueTestTable.evaluateUnique(input)
    val expected: UniqueEvalResult[Input, Output] = UniqueEvalResult.NotUnique(diagnostics(false, true, true))
    assertEquals(result, expected)
  }

  val anyTestTable: DecisionTable[Input, Output, HitPolicy.Any] = uniqueTestTable.copy(hitPolicy = HitPolicy.Any)

  test("any - single matching rule") {
    val result                                 = anyTestTable.evaluateAny(Input(2))
    val expected: AnyEvalResult[Input, Output] = AnyEvalResult.Success(rawResults(false, false, true), Output(1))
    assertEquals(result, expected)
  }

  test("any - no matching rules") {
    val input                                  = Input[Value](0)
    val result                                 = anyTestTable.evaluateAny(input)
    val expected: AnyEvalResult[Input, Output] = AnyEvalResult.NoHit(rawResults(false, false, false))
    assertEquals(result, expected)
  }

  test("any - multiple matching rules with same value") {
    val input                                  = Input[Value](3)
    val result                                 = anyTestTable.evaluateAny(input)
    val expected: AnyEvalResult[Input, Output] = AnyEvalResult.Success(rawResults(false, true, true), Output(1))
    assertEquals(result, expected)
  }

  test("any - multiple matching rules with different values") {
    val input                                  = Input[Value](4)
    val result                                 = anyTestTable.evaluateAny(input)
    val expected: AnyEvalResult[Input, Output] = AnyEvalResult.NotUnique(rawResults(true, true, true))
    assertEquals(result, expected)
  }

  val firstTestTable: DecisionTable[Input, Output, HitPolicy.First] = uniqueTestTable.copy(hitPolicy = HitPolicy.First)

  test("first - no matching rules") {
    val result                                   = firstTestTable.evaluateFirst(Input(1))
    val expected: FirstEvalResult[Input, Output] = FirstEvalResult(rawResults(false, false, false), None)
    assertEquals(result, expected)
  }
  test("first - 3rd rule") {
    val result                                   = firstTestTable.evaluateFirst(Input(2))
    val expected: FirstEvalResult[Input, Output] = FirstEvalResult(rawResults(false, false, true), Some(Output(1)))
    assertEquals(result, expected)
  }
  test("first - 1st rule") {
    val result                                   = firstTestTable.evaluateFirst(Input(4))
    val expected: FirstEvalResult[Input, Output] = FirstEvalResult(rawResults(true), Some(Output(2)))
    assertEquals(result, expected)
  }

  val collectTestTable: DecisionTable[Input, Output, HitPolicy.Collect] = uniqueTestTable.copy(hitPolicy = HitPolicy.Collect)

  test("collect - no matching rules") {
    val result                                     = collectTestTable.evaluateCollect(Input(1))
    val expected: CollectEvalResult[Input, Output] = CollectEvalResult(rawResults(false, false, false), List())
    assertEquals(result, expected)
  }
  test("collect - all rules") {
    val result                                     = collectTestTable.evaluateCollect(Input(4))
    val expected: CollectEvalResult[Input, Output] = CollectEvalResult(rawResults(true, true, true), List(Output(2), Output(1), Output(1)))
    assertEquals(result, expected)
  }

  val collectSumTable: DecisionTable[Input, Output, HitPolicy.CollectSum] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectSum)

  test("collect sum - no matching rules") {
    val result                                        = collectSumTable.evaluateCollectSum(Input(1))((a, b) => Output(a.c + b.c))
    val expected: CollectSumEvalResult[Input, Output] = CollectSumEvalResult(rawResults(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect sum - all matching rules") {
    val result                                        = collectSumTable.evaluateCollectSum(Input(4))((a, b) => Output(a.c + b.c))
    val expected: CollectSumEvalResult[Input, Output] = CollectSumEvalResult(rawResults(true, true, true), Some(Output(4)))
    assertEquals(result, expected)
  }

  val collectMinTable: DecisionTable[Input, Output, HitPolicy.CollectMin] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMin)

  test("collect min - no matching rules") {
    val result                                          = collectMinTable.evaluateCollectMin(Input(1))(using Ordering.by(_.c))
    val expected: CollectBoundEvalResult[Input, Output] = CollectBoundEvalResult(rawResults(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect min - all matching rules") {
    val result                                          = collectMinTable.evaluateCollectMin(Input(4))(using Ordering.by(_.c))
    val expected: CollectBoundEvalResult[Input, Output] = CollectBoundEvalResult(rawResults(true, true, true), Some(Output(1)))
    assertEquals(result, expected)
  }
  val collectMaxTable: DecisionTable[Input, Output, HitPolicy.CollectMax] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMax)

  test("collect max - no matching rules") {
    val result                                          = collectMaxTable.evaluateCollectMax(Input(1))(using Ordering.by(_.c))
    val expected: CollectBoundEvalResult[Input, Output] = CollectBoundEvalResult(rawResults(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect max - all matching rules") {
    val result                                          = collectMaxTable.evaluateCollectMax(Input(4))(using Ordering.by(_.c))
    val expected: CollectBoundEvalResult[Input, Output] = CollectBoundEvalResult(rawResults(true, true, true), Some(Output(2)))
    assertEquals(result, expected)
  }

  val collectCountTable: DecisionTable[Input, Output, HitPolicy.CollectCount] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectCount)

  test("collect count - no matching rules") {
    val result                                          = collectCountTable.evaluateCollectCount(Input(1))
    val expected: CollectCountEvalResult[Input, Output] = CollectCountEvalResult(rawResults(false, false, false), 0)
    assertEquals(result, expected)
  }

  test("collect count - 2 matching rules") {
    val result                                          = collectCountTable.evaluateCollectCount(Input(3))
    val expected: CollectCountEvalResult[Input, Output] = CollectCountEvalResult(rawResults(false, true, true), 2)
    assertEquals(result, expected)
  }

  def rawResults(hits: Boolean*): List[Rule.Result[Input, Output]] = {
    hits.zipWithIndex
      .map((wasHit, idx) =>
        Rule.Result(
          Input(wasHit),
          if (wasHit) EvaluationResult.Satisfied(uniqueTestTable.rules(idx).evaluateOutput())
          else EvaluationResult.NotSatisfied(),
        ),
      )
      .toList
  }
  def diagnostics(hits: Boolean*): EvalDiagnostics[Input, Output]  = {
    val rawResults = hits.zipWithIndex
      .map((wasHit, idx) =>
        Rule.Result(
          Input(wasHit),
          if (wasHit) EvaluationResult.Satisfied(uniqueTestTable.rules(idx).evaluateOutput())
          else EvaluationResult.NotSatisfied(),
        ),
      )
      .toList
    EvalDiagnostics(rawResults, ???, ???)
  }

}
