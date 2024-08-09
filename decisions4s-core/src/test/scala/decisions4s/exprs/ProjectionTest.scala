package decisions4s.exprs

import decisions4s.*
import org.scalatest.freespec.AnyFreeSpec
class ProjectionTest extends AnyFreeSpec {

  case class Foo[F[_]](x: F[Int]) derives HKD

  "basic" in {
    val raw                      = Foo[Expr](Literal(1))
    val fooExpr: Expr[Foo[Expr]] = Literal(raw)
    val projection               = fooExpr.projection.x
    assert(projection.renderExpression == """{
                                            |  x: 1
                                            |}.x""".stripMargin)
    TestUtils.checkExpression(projection, 1)
  }
}
