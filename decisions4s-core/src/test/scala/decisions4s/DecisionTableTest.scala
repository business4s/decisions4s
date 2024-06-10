package decisions4s

import munit.FunSuite

class DecisionTableTest extends FunSuite {

  case class Input[F[_]](a: F[Int]) derives HKD
  case class Output[F[_]](c: F[Int]) derives HKD

  val uniqueTestTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
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
    "test",
    HitPolicy.Single,
  )

  test("single - single matching rule") {
    val result: EvalResult.Single[Input, Output]   = uniqueTestTable.evaluateSingle(Input(2))
    val expected: EvalResult.Single[Input, Output] = buildExpectedResult(List(false, false, true), Right(Some(Output(1))))
    assertEquals(result, expected)
  }

  test("single - no matching rules") {
    val input                                      = Input[Value](0)
    val result                                     = uniqueTestTable.evaluateSingle(input)
    val expected: EvalResult.Single[Input, Output] = buildExpectedResult(List(false, false, false), Right(None))
    assertEquals(result, expected)
  }

  test("single - multiple matching rules") {
    val input                                      = Input[Value](3)
    val result                                     = uniqueTestTable.evaluateSingle(input)
    val expected: EvalResult.Single[Input, Output] = buildExpectedResult(List(false, true, true), Left("not-single"))
    assertEquals(result, expected)
  }

  val anyTestTable: DecisionTable[Input, Output, HitPolicy.Distinct] = uniqueTestTable.copy(hitPolicy = HitPolicy.Distinct)

  test("distinct - single matching rule") {
    val result                                       = anyTestTable.evaluateDistinct(Input(2))
    val expected: EvalResult.Distinct[Input, Output] = buildExpectedResult(List(false, false, true), Right(Some(Output(1))))
    assertEquals(result, expected)
  }

  test("distinct - no matching rules") {
    val input                                        = Input[Value](0)
    val result                                       = anyTestTable.evaluateDistinct(input)
    val expected: EvalResult.Distinct[Input, Output] = buildExpectedResult(List(false, false, false), Right(None))
    assertEquals(result, expected)
  }

  test("distinct - multiple matching rules with same value") {
    val input                                        = Input[Value](3)
    val result                                       = anyTestTable.evaluateDistinct(input)
    val expected: EvalResult.Distinct[Input, Output] = buildExpectedResult(List(false, true, true), Right(Some(Output(1))))
    assertEquals(result, expected)
  }

  test("distinct - multiple matching rules with different values") {
    val input                                        = Input[Value](4)
    val result                                       = anyTestTable.evaluateDistinct(input)
    val expected: EvalResult.Distinct[Input, Output] = buildExpectedResult(List(true, true, true), Left("not-distinct"))
    assertEquals(result, expected)
  }

  val firstTestTable: DecisionTable[Input, Output, HitPolicy.First] = uniqueTestTable.copy(hitPolicy = HitPolicy.First)

  test("first - no matching rules") {
    val result                                    = firstTestTable.evaluateFirst(Input(1))
    val expected: EvalResult.First[Input, Output] = buildExpectedResult(List(false, false, false), None)
    assertEquals(result, expected)
  }
  test("first - 3rd rule") {
    val result                                    = firstTestTable.evaluateFirst(Input(2))
    val expected: EvalResult.First[Input, Output] = buildExpectedResult(List(false, false, true), Some(Output(1)))
    assertEquals(result, expected)
  }
  test("first - 1st rule") {
    val result                                    = firstTestTable.evaluateFirst(Input(4))
    val expected: EvalResult.First[Input, Output] = buildExpectedResult(List(true), Some(Output(2)))
    assertEquals(result, expected)
  }

  val collectTestTable: DecisionTable[Input, Output, HitPolicy.Collect] = uniqueTestTable.copy(hitPolicy = HitPolicy.Collect)

  test("collect - no matching rules") {
    val result                                      = collectTestTable.evaluateCollect(Input(1))
    val expected: EvalResult.Collect[Input, Output] = buildExpectedResult(List(false, false, false), List())
    assertEquals(result, expected)
  }
  test("collect - all rules") {
    val result                                      = collectTestTable.evaluateCollect(Input(4))
    val expected: EvalResult.Collect[Input, Output] = buildExpectedResult(List(true, true, true), List(Output(2), Output(1), Output(1)))
    assertEquals(result, expected)
  }

  val collectSumTable: DecisionTable[Input, Output, HitPolicy.CollectSum] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectSum)

  test("collect sum - no matching rules") {
    val result                                  = collectSumTable.evaluateSum(Input(1))((a, b) => Output(a.c + b.c))
    val expected: EvalResult.Sum[Input, Output] = buildExpectedResult(List(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect sum - all matching rules") {
    val result                                  = collectSumTable.evaluateSum(Input(4))((a, b) => Output(a.c + b.c))
    val expected: EvalResult.Sum[Input, Output] = buildExpectedResult(List(true, true, true), Some(Output(4)))
    assertEquals(result, expected)
  }

  val collectMinTable: DecisionTable[Input, Output, HitPolicy.CollectMin] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMin)

  test("collect min - no matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                                  = collectMinTable.evaluateMin(Input(1))
    val expected: EvalResult.Min[Input, Output] = buildExpectedResult(List(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect min - all matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                                  = collectMinTable.evaluateMin(Input(4))
    val expected: EvalResult.Min[Input, Output] = buildExpectedResult(List(true, true, true), Some(Output(1)))
    assertEquals(result, expected)
  }
  val collectMaxTable: DecisionTable[Input, Output, HitPolicy.CollectMax] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMax)

  test("collect max - no matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                                  = collectMaxTable.evaluateMax(Input(1))
    val expected: EvalResult.Max[Input, Output] = buildExpectedResult(List(false, false, false), None)
    assertEquals(result, expected)
  }
  test("collect max - all matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                                  = collectMaxTable.evaluateMax(Input(4))
    val expected: EvalResult.Max[Input, Output] = buildExpectedResult(List(true, true, true), Some(Output(2)))
    assertEquals(result, expected)
  }

  val collectCountTable: DecisionTable[Input, Output, HitPolicy.CollectCount] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectCount)

  test("collect count - no matching rules") {
    val result                                    = collectCountTable.evaluateCount(Input(1))
    val expected: EvalResult.Count[Input, Output] = buildExpectedResult(List(false, false, false), 0)
    assertEquals(result, expected)
  }

  test("collect count - 2 matching rules") {
    val result                                    = collectCountTable.evaluateCount(Input(3))
    val expected: EvalResult.Count[Input, Output] = buildExpectedResult(List(false, true, true), 2)
    assertEquals(result, expected)
  }

  def buildExpectedResult[T](rulesHit: List[Boolean], output: T): EvalResult[Input, Output, T] = ???

//  def rawResults(hits: Boolean*): List[Rule.Result[Input, Output]] = {
//    hits.zipWithIndex
//      .map((wasHit, idx) =>
//        Rule.Result(
//          Input(wasHit),
//          if (wasHit) EvaluationResult.Satisfied(uniqueTestTable.rules(idx).evaluateOutput())
//          else EvaluationResult.NotSatisfied(),
//        ),
//      )
//      .toList
//  }
//  def diagnostics(hits: Boolean*): EvalDiagnostics[Input, Output]  = {
//    val rawResults = hits.zipWithIndex
//      .map((wasHit, idx) =>
//        Rule.Result(
//          Input(wasHit),
//          if (wasHit) EvaluationResult.Satisfied(uniqueTestTable.rules(idx).evaluateOutput())
//          else EvaluationResult.NotSatisfied(),
//        ),
//      )
//      .toList
//    EvalDiagnostics(rawResults, ???, ???)
//  }

}
