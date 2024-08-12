package decisions4s

import org.scalatest.freespec.AnyFreeSpec

class NestedStructuresTest extends AnyFreeSpec {

  case class Foo[F[_]](a: F[Int], b: F[Int]) derives HKD
  case class Bar[F[_]](c: F[Int]) derives HKD
  case class Baz[F[_]](d: F[Int], e: F[Int], f: F[Int])

  case class Input[F[_]](foo: F[Foo[F]], bar: F[Bar[F]]) derives HKD
  case class Output[F[_]](c: F[Baz[F]]) derives HKD

  val testTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(
          foo = wholeInput.foo.prj.a.equalsTo(1),
          bar = it.catchAll,
        ),
        output = Output(Baz[OutputValue](1, wholeInput.bar.projection.c, 4)),
      ),
    ),
    "test",
    HitPolicy.Single,
  )

  "basics" in {
    val input                                    = Input[Value](Foo(1, 2), Bar(3))
    val result: EvalResult.Single[Input, Output] = testTable.evaluateSingle(input)
    assert(result.output == Right(Some(Output[Value](Baz(1, 3, 4)))))

    given EvaluationContext[Input] = EvaluationContext.stub

    assert(testTable.rules.head.matching.foo.renderExpression == "foo.a = 1")
    assert(testTable.rules.head.output.c.renderExpression == """{
                                                               |  d: 1,
                                                               |  e: bar.c,
                                                               |  f: 4
                                                               |}""".stripMargin)
  }

}
