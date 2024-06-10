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
    assertEquals(result.output, Right(Some(Output[Value](1))))
    assertEquals(result.results, rawResults(false, false, true))
  }

  test("single - no matching rules") {
    val input  = Input[Value](0)
    val result = uniqueTestTable.evaluateSingle(input)
    assertEquals(result.output, Right(None))
    assertEquals(result.results, rawResults(false, false, false))
  }

  test("single - multiple matching rules") {
    val input                                      = Input[Value](3)
    val result                                     = uniqueTestTable.evaluateSingle(input)
    assertEquals(result.output, Left("not-single"): Either["not-single", Option[Output[Value]]])
    assertEquals(result.results, rawResults(false, true, true))
  }

  val anyTestTable: DecisionTable[Input, Output, HitPolicy.Distinct] = uniqueTestTable.copy(hitPolicy = HitPolicy.Distinct)

  test("distinct - single matching rule") {
    val result                                       = anyTestTable.evaluateDistinct(Input(2))
    assertEquals(result.output, Right(Some(Output[Value](1))))
    assertEquals(result.results, rawResults(false, false, true))
  }

  test("distinct - no matching rules") {
    val input  = Input[Value](0)
    val result = anyTestTable.evaluateDistinct(input)
    assertEquals(result.output, Right(None))
    assertEquals(result.results, rawResults(false, false, false))
  }

  test("distinct - multiple matching rules with same value") {
    val input  = Input[Value](3)
    val result = anyTestTable.evaluateDistinct(input)
    assertEquals(result.output, Right(Some(Output[Value](1))))
    assertEquals(result.results, rawResults(false, true, true))
  }

  test("distinct - multiple matching rules with different values") {
    val input  = Input[Value](4)
    val result = anyTestTable.evaluateDistinct(input)
    assertEquals(result.output, Left("not-distinct"): Either["not-distinct", Option[Output[Value]]])
    assertEquals(result.results, rawResults(true, true, true))
  }

  val firstTestTable: DecisionTable[Input, Output, HitPolicy.First] = uniqueTestTable.copy(hitPolicy = HitPolicy.First)

  test("first - no matching rules") {
    val result = firstTestTable.evaluateFirst(Input(1))
    assertEquals(result.output, None)
    assertEquals(result.results, rawResults(false, false, false))
  }
  test("first - 3rd rule") {
    val result = firstTestTable.evaluateFirst(Input(2))
    assertEquals(result.output, Some(Output[Value](1)))
    assertEquals(result.results, rawResults(false, false, true))
  }
  test("first - 1st rule") {
    val result = firstTestTable.evaluateFirst(Input(4))
    assertEquals(result.output, Some(Output[Value](2)))
    assertEquals(result.results, rawResults(true))
  }

  val collectTestTable: DecisionTable[Input, Output, HitPolicy.Collect] = uniqueTestTable.copy(hitPolicy = HitPolicy.Collect)

  test("collect - no matching rules") {
    val result = collectTestTable.evaluateCollect(Input(1))
    assertEquals(result.output, List())
    assertEquals(result.results, rawResults(false, false, false))
  }
  test("collect - all rules") {
    val result = collectTestTable.evaluateCollect(Input(4))
    assertEquals(result.output, List(Output[Value](2), Output[Value](1), Output[Value](1)))
    assertEquals(result.results, rawResults(true, true, true))
  }

  val collectSumTable: DecisionTable[Input, Output, HitPolicy.CollectSum] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectSum)

  test("collect sum - no matching rules") {
    val result = collectSumTable.evaluateSum(Input(1))((a, b) => Output(a.c + b.c))
    assertEquals(result.output, None)
    assertEquals(result.results, rawResults(false, false, false))
  }
  test("collect sum - all matching rules") {
    val result = collectSumTable.evaluateSum(Input(4))((a, b) => Output(a.c + b.c))
    assertEquals(result.output, Some(Output[Value](4)))
    assertEquals(result.results, rawResults(true, true, true))
  }

  val collectMinTable: DecisionTable[Input, Output, HitPolicy.CollectMin] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMin)

  test("collect min - no matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMinTable.evaluateMin(Input(1))
    assertEquals(result.output, None)
    assertEquals(result.results, rawResults(false, false, false))
  }
  test("collect min - all matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMinTable.evaluateMin(Input(4))
    assertEquals(result.output, Some(Output[Value](1)))
    assertEquals(result.results, rawResults(true, true, true))
  }
  val collectMaxTable: DecisionTable[Input, Output, HitPolicy.CollectMax] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMax)

  test("collect max - no matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMaxTable.evaluateMax(Input(1))
    assertEquals(result.output, None)
    assertEquals(result.results, rawResults(false, false, false))
  }
  test("collect max - all matching rules") {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMaxTable.evaluateMax(Input(4))
    assertEquals(result.output, Some(Output[Value](2)))
    assertEquals(result.results, rawResults(true, true, true))
  }

  val collectCountTable: DecisionTable[Input, Output, HitPolicy.CollectCount] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectCount)

  test("collect count - no matching rules") {
    val result = collectCountTable.evaluateCount(Input(1))
    assertEquals(result.output, 0)
    assertEquals(result.results, rawResults(false, false, false))
  }

  test("collect count - 2 matching rules") {
    val result = collectCountTable.evaluateCount(Input(3))
    assertEquals(result.output, 2)
    assertEquals(result.results, rawResults(false, true, true))
  }

  def buildExpectedResult[T](rulesHit: List[Boolean], output: T): EvalResult[Input, Output, T] = ???

  def rawResults(hits: Boolean*): List[Rule.Result[Input, Output]] = {
    hits.zipWithIndex
      .map((wasHit, idx) =>
        Rule.Result(
          Input(wasHit),
          if (wasHit) Some(uniqueTestTable.rules(idx).evaluateOutput())
          else None,
        ),
      )
      .toList
  }

}
