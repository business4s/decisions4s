package decisions4s

trait Expr[+Out] {
  def evaluate: Out
  def describe: String
}
