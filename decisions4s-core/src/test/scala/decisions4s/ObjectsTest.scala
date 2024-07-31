package decisions4s

import decisions4s.HKD.FieldUtils
import org.scalatest.freespec.AnyFreeSpec

import scala.util.chaining.scalaUtilChainingOps

class ObjectsTest extends AnyFreeSpec {

  case class Foo[F[_]](a: F[Int], b: F[Int]) derives HKD
  case class Bar[F[_]](c: F[Int]) derives HKD
  case class Baz[F[_]](d: F[Int], e: F[Int], f: F[Int])

  case class Input[F[_]](foo: F[Foo[F]], bar: F[Bar[F]]) derives HKD
  case class Output[F[_]](c: F[Baz[F]]) derives HKD


  // <foo>: Foo(...)


  class Projection[-In, O1, +O2](base: Expr[In, O1], get: O1 => Expr[In, O2], label: String) extends Expr[In, O2] {
    override def evaluate(in: In): O2 = base.evaluate(in).pipe(get).evaluate(in)

    override def renderExpression: String = s"${base.renderExpression}.$label"
  }


  def projection[In, Data[_[_]]](in: ValueExpr[Data[ValueExpr]])(using hkd: HKD[Data]): Data[[t] =>> Expr[In, t]] = {
    hkd.construct([t] => (fu: FieldUtils[Data, t]) => Projection[In, Data[ValueExpr], t](in, fu.extract, fu .name))
  }

  extension [In, Data[_[_]]](in: ValueExpr[Data[ValueExpr]]) {

    def inside(using HKD[Data]): Data[ValueExpr] = projection(in)

  }


  val testTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
    rules = List(
      Rule(
        matching = Input(
          foo = wholeInput.foo.inside.a.equalsTo(1),
          bar = it.catchAll
        ),
        output = ???
//        output = Output[OutputExpr[Input]](Baz[OutputExpr[Input]](1,2,3)),
      ),
    ),
    "test",
    HitPolicy.Single,
  )

  "basics" in {
    val input = Input[Value](Foo(1, 2), Bar(3))
    val result: EvalResult.Single[Input, Output] = testTable.evaluateSingle(input)
    assert(result.output == Right(Some(Output[Value](Baz(1,2,3)))))
  }

}