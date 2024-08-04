package decisions4s.exprs

import decisions4s.*
import munit.FunSuite

class ProjectionTest extends FunSuite {

  case class Foo[F[_]](x: F[Int]) derives HKD

  test("basic") {
    val raw                      = Foo[Expr](Literal(1))
    val fooExpr: Expr[Foo[Expr]] = Literal(raw)
    val projection               = fooExpr.projection.x
    assert(projection.renderExpression == """{
                                            |  x: 1
                                            |}.x""".stripMargin)
    TestUtils.checkExpression(projection, 1)
  }
}
