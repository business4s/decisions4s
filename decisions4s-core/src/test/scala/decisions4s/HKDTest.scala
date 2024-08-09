package decisions4s

import decisions4s.internal.Meta
import org.scalatest.freespec.AnyFreeSpec
import shapeless3.deriving.Const

class HKDTest extends AnyFreeSpec {

  case class TestSubject[F[_]](a: F[Int], b: F[Int]) derives HKD

  val hkd = summon[HKD[TestSubject]]

  "mapK" in {
    val a                             = TestSubject[Option](Some(1), None)
    val b: TestSubject[Const[String]] = a.mapK[Const[String]]([t] => (x: Option[t]) => x.toString)
    assert(b == TestSubject[Const[String]]("Some(1)", "None"))
  }

  "map2" in {
    val a = TestSubject[Option](Some(1), None)
    val b = TestSubject[Option](None, Some(2))
    val c = HKD.map2(a, b)([ t] => (f1, f2) => f1.orElse(f2))
    assert(c == TestSubject(Some(1), Some(2)))
  }

  "pure" in {
    val a = hkd.pure[Const[Int]]([t] => () => 1)
    assert(a == TestSubject[Const[Int]](1, 1))
  }

  "fieldNames" in {
    assert(hkd.fieldNames == List("a", "b"))
  }

  "indices" in {
    assert(hkd.indices == TestSubject[Const[Int]](0, 1))
  }

  "meta" in {
    assert(hkd.meta == TestSubject[Meta](Meta(0, "a", None), Meta(1, "b", None)))
  }

  "construct" in {
    val a = TestSubject[Option](Some(1), None)
    val b = hkd.construct[Const[String]]([t] => (fu: HKD.FieldUtils[TestSubject, t]) => fu.extract(a).toString)
    assert(b == TestSubject[Const[String]]("Some(1)", "None"))
  }

}
