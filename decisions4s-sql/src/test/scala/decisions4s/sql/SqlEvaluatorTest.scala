package decisions4s.sql

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import decisions4s._
import decisions4s.exprs._
import doobie.implicits._

class SqlEvaluatorTest extends AnyFreeSpec with Matchers {
  import SqlEvaluator._

  extension (frag: doobie.Fragment) {
    def str: String = frag.query[Int].sql
  }

  "SqlEvaluator" - {
    "should compile literals" in {
       toSql(Literal(1)).str shouldBe "?"
       toSql(Literal("foo")).str shouldBe "?"
    }

    "should compile arithmetic" in {
       val expr = Literal(1) + Literal(2)
       toSql(expr).str shouldBe "(? + ?)"
    }

    "should compile variables" in {
       val expr = Variable[Int]("x", 42)
       toSql(expr).str shouldBe "x"
    }

    "should compile boolean logic" in {
       val expr = (Variable[Int]("x", 1) > 5) && (Variable[Int]("y", 2) < 10)
       toSql(expr).str shouldBe "((x > ?) AND (y < ?))"
    }
    
    "should compile UnaryTest (Input Entry)" in {
       val x = Variable[Int]("x", 3)
       val test: UnaryTest[Int] = UnaryTest.Compare(UnaryTest.Compare.Sign.>, Literal(5))
       toSql(In(x, test)).str shouldBe "(x > ?)"
    }
    
     "should compile UnaryTest EqualTo" in {
       val x = Variable[Int]("x", 3)
       val test = UnaryTest.EqualTo(Literal(5))
       toSql(In(x, test)).str shouldBe "(x = ?)"
    }
    
    "should compile Function1" in {
       val x = Variable[Int]("x", 1)
       val f = Function[Int]("myFunc")(x)(_ + 1)
       toSql(f).str shouldBe "myFunc(x)"
    }
  }
}
