package decisions4s

trait Expr[-In, +Out] {
  def evaluate(in: In): Out
  def renderFeelExpression: String
}
