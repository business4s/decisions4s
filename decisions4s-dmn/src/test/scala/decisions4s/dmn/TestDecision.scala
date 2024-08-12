package decisions4s.dmn

import decisions4s.*
import decisions4s.exprs.VariableStub

import scala.util.Random

object TestDecision {

  case class Input[F[_]](a: F[Int], b: F[Int]) derives HKD
  case class Output[F[_]](b: F[Int], c: F[Int]) derives HKD

  val table = DecisionTable(
    List.fill(2)(
      Rule(
        matching = Input(randomExpr(), randomExpr()),
        output = Output(randomExpr(), randomExpr()),
        annotation = Some(randomString()),
      ),
    ),
    "TableName",
    HitPolicy.First,
  )

  lazy val random              = new Random(1)
  def randomString(): String   = random.alphanumeric.take(10).mkString
  def randomExpr[T](): Expr[T] = VariableStub(randomString())

}
