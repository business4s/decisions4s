package decisions4s

import org.scalatest.freespec.AnyFreeSpec

class DecisionTableTest extends AnyFreeSpec {

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

  "single - single matching rule" in {
    val result: EvalResult.Single[Input, Output] = uniqueTestTable.evaluateSingle(Input(2))
    assert(result.output == Right(Some(Output[Value](1))))
    assert(result.results == rawResults(false, false, true))
  }

  "single - no matching rules" in {
    val input  = Input[Value](0)
    val result = uniqueTestTable.evaluateSingle(input)
    assert(result.output == Right(None))
    assert(result.results == rawResults(false, false, false))
  }

  "single - multiple matching rules" in {
    val input  = Input[Value](3)
    val result = uniqueTestTable.evaluateSingle(input)
    assert(result.output == Left("not-single"))
    assert(result.results == rawResults(false, true, true))
  }

  val anyTestTable: DecisionTable[Input, Output, HitPolicy.Distinct] = uniqueTestTable.copy(hitPolicy = HitPolicy.Distinct)

  given Equiv[Output[Value]] = Equiv.universal

  "distinct - single matching rule" in {
    val result = anyTestTable.evaluateDistinct(Input(2))
    assert(result.output == Right(Some(Output[Value](1))))
    assert(result.results == rawResults(false, false, true))
  }

  "distinct - no matching rules" in {
    val input  = Input[Value](0)
    val result = anyTestTable.evaluateDistinct(input)
    assert(result.output == Right(None))
    assert(result.results == rawResults(false, false, false))
  }

  "distinct - multiple matching rules with same value" in {
    val input  = Input[Value](3)
    val result = anyTestTable.evaluateDistinct(input)
    assert(result.output == Right(Some(Output[Value](1))))
    assert(result.results == rawResults(false, true, true))
  }

  "distinct - multiple matching rules with different values" in {
    val input  = Input[Value](4)
    val result = anyTestTable.evaluateDistinct(input)
    assert(result.output == Left("not-distinct"))
    assert(result.results == rawResults(true, true, true))
  }

  "distinct - equality is customizable via Equiv (e.g. bit-exact Double)" in {
    case class FloatInput[F[_]](trigger: F[Int]) derives HKD
    case class FloatOutput[F[_]](value: F[Double]) derives HKD

    val table: DecisionTable[FloatInput, FloatOutput, HitPolicy.Distinct] = DecisionTable(
      rules = List(
        Rule(matching = FloatInput(it > 0), output = FloatOutput(+0.0)),
        Rule(matching = FloatInput(it > 0), output = FloatOutput(-0.0)),
      ),
      "float-distinct",
      HitPolicy.Distinct,
    )

    // Universal Equiv uses ==; case-class equality on a Double field uses primitive ==,
    // which treats +0.0 and -0.0 as the same value.
    locally {
      given Equiv[FloatOutput[Value]] = Equiv.universal
      val result                      = table.evaluateDistinct(FloatInput(1))
      assert(result.output == Right(Some(FloatOutput[Value](+0.0))))
    }

    // A user-supplied Equiv comparing raw bits flags +0.0 vs -0.0 as not-distinct.
    locally {
      given Equiv[FloatOutput[Value]] = (a, b) => java.lang.Double.doubleToRawLongBits(a.value) == java.lang.Double.doubleToRawLongBits(b.value)
      val result                      = table.evaluateDistinct(FloatInput(1))
      assert(result.output == Left("not-distinct"))
    }
  }

  val firstTestTable: DecisionTable[Input, Output, HitPolicy.First] = uniqueTestTable.copy(hitPolicy = HitPolicy.First)

  "first - no matching rules" in {
    val result = firstTestTable.evaluateFirst(Input(1))
    assert(result.output == None)
    assert(result.results == rawResults(false, false, false))
  }
  "first - 3rd rule" in {
    val result = firstTestTable.evaluateFirst(Input(2))
    assert(result.output == Some(Output[Value](1)))
    assert(result.results == rawResults(false, false, true))
  }
  "first - 1st rule" in {
    val result = firstTestTable.evaluateFirst(Input(4))
    assert(result.output == Some(Output[Value](2)))
    assert(result.results == rawResults(true))
  }

  val collectTestTable: DecisionTable[Input, Output, HitPolicy.Collect] = uniqueTestTable.copy(hitPolicy = HitPolicy.Collect)

  "collect - no matching rules" in {
    val result = collectTestTable.evaluateCollect(Input(1))
    assert(result.output == List())
    assert(result.results == rawResults(false, false, false))
  }
  "collect - all rules" in {
    val result = collectTestTable.evaluateCollect(Input(4))
    assert(result.output == List(Output[Value](2), Output[Value](1), Output[Value](1)))
    assert(result.results == rawResults(true, true, true))
  }

  val collectSumTable: DecisionTable[Input, Output, HitPolicy.CollectSum] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectSum)

  "collect sum - no matching rules" in {
    val result = collectSumTable.evaluateSum(Input(1))((a, b) => Output(a.c + b.c))
    assert(result.output == None)
    assert(result.results == rawResults(false, false, false))
  }
  "collect sum - all matching rules" in {
    val result = collectSumTable.evaluateSum(Input(4))((a, b) => Output(a.c + b.c))
    assert(result.output == Some(Output[Value](4)))
    assert(result.results == rawResults(true, true, true))
  }

  val collectMinTable: DecisionTable[Input, Output, HitPolicy.CollectMin] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMin)

  "collect min - no matching rules" in {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMinTable.evaluateMin(Input(1))
    assert(result.output == None)
    assert(result.results == rawResults(false, false, false))
  }
  "collect min - all matching rules" in {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMinTable.evaluateMin(Input(4))
    assert(result.output == Some(Output[Value](1)))
    assert(result.results == rawResults(true, true, true))
  }
  val collectMaxTable: DecisionTable[Input, Output, HitPolicy.CollectMax] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectMax)

  "collect max - no matching rules" in {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMaxTable.evaluateMax(Input(1))
    assert(result.output == None)
    assert(result.results == rawResults(false, false, false))
  }
  "collect max - all matching rules" in {
    given Ordering[Output[Value]] = Ordering.by(_.c)
    val result                    = collectMaxTable.evaluateMax(Input(4))
    assert(result.output == Some(Output[Value](2)))
    assert(result.results == rawResults(true, true, true))
  }

  val collectCountTable: DecisionTable[Input, Output, HitPolicy.CollectCount] = uniqueTestTable.copy(hitPolicy = HitPolicy.CollectCount)

  "collect count - no matching rules" in {
    val result = collectCountTable.evaluateCount(Input(1))
    assert(result.output == 0)
    assert(result.results == rawResults(false, false, false))
  }

  "collect count - 2 matching rules" in {
    val result = collectCountTable.evaluateCount(Input(3))
    assert(result.output == 2)
    assert(result.results == rawResults(false, true, true))
  }

  "wholeInput" in {
    case class Input[F[_]](a: F[Int], b: F[Int]) derives HKD
    case class Output[F[_]](c: F[Int]) derives HKD
    val table: DecisionTable[Input, Output, HitPolicy.Single.type] = DecisionTable(
      rules = List(
        Rule(
          matching = ctx ?=>
            Input(
              a = !it.equalsTo(ctx.wholeInput.b),
              b = !it.equalsTo(wholeInput.a),
            ),
          output = Output(
            c = wholeInput.a + wholeInput.b,
          ),
        ),
      ),
      "test",
      HitPolicy.Single,
    )

    val result = table.evaluateSingle(Input[Value](1, 3))
    assert(result.output == Right(Some(Output[Value](4))))

    val result2 = table.evaluateSingle(Input[Value](1, 1))
    assert(result2.output == Right(None))
  }

  def buildExpectedResult[T](rulesHit: List[Boolean], output: T): EvalResult[Input, Output, T] = ???

  def rawResults(hits: Boolean*): List[RuleResult[Input, Output]] = {
    given EvaluationContext[Input] = new EvaluationContext[Input] {
      override def wholeInput: Input[Expr] = null // variables not used in those tests
    }

    hits.zipWithIndex
      .map((wasHit, idx) =>
        RuleResult(
          Input(wasHit),
          if (wasHit) Some(uniqueTestTable.rules(idx).evaluateOutput())
          else None,
        ),
      )
      .toList
  }

}
