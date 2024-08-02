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

  class Projection[O1, +O2](base: Expr[O1], get: O1 => Expr[O2], label: String) extends Expr[O2] {
    override def evaluate: O2             = base.evaluate.pipe(get).evaluate
    override def renderExpression: String = s"${base.renderExpression}.$label"
  }

  def projection[Data[_[_]]](in: Expr[Data[Expr]])(using hkd: HKD[Data]): Data[[t] =>> Expr[t]] = {
    hkd.construct([t] => (fu: FieldUtils[Data, t]) => Projection[Data[Expr], t](in, fu.extract, fu.name))
  }

  extension [Data[_[_]]](in: Expr[Data[Expr]]) {

    def project(using HKD[Data]): Data[Expr] = projection(in)

  }

  val testTable: DecisionTable[Input, Output, HitPolicy.Single] = DecisionTable(
    rules = List(
      Rule(
        matching = ctx ?=> Input(
          foo = wholeInput.foo.project.a.equalsTo(1),
          bar = it.catchAll,
        ),
        output = Output(Baz[OutputValue](1, 2, 3)),
      ),
    ),
    "test",
    HitPolicy.Single,
  )

  "basics" in {
    val input                                    = Input[Value](Foo(1, 2), Bar(3))
    val result: EvalResult.Single[Input, Output] = testTable.evaluateSingle(input)
    assert(result.output == Right(Some(Output[Value](Baz(1, 2, 3)))))
    println(result.makeDiagnosticsString)
  }

}
