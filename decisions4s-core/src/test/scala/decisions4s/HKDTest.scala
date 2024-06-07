package decisions4s

import org.scalatest.freespec.AnyFreeSpec
import shapeless3.deriving.Const

class HKDTest extends AnyFreeSpec {

  case class TestSubject[F[_]](a: F[Int], b: F[Int]) derives HKD

  "basics" in {
    val a = TestSubject[Option](Some(1), None)

    val b: TestSubject[Const[String]] = a.mapK[Const[String]]([t] => (x: Option[t]) => x.toString)
    assert(b == TestSubject[Const[String]]("Some(1)", "None"))

    val c: TestSubject[[t] =>> (Option[t], String)] = a.productK(b)
    assert(c == TestSubject[[t] =>> (Option[t], String)]((Some(1), "Some(1)"), (None, "None")))

    val hkd = summon[HKD[TestSubject]]

    val d = hkd.pure[Const[Int]]([t] => () => 1)
    assert(d == TestSubject[Const[Int]](1, 1))

    assert(hkd.fieldNames == List("a", "b"))

  }

}
