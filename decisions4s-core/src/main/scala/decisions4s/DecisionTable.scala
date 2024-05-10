package decisions4s

import cats.syntax.all.*

case class DecisionTable[Input[_[_]], Output[_[_]]](
    rules: List[Rule[Input, Output]],
    inputNames: Input[Name],
    outputNames: Output[Name],
    name: Option[String] = None,
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] =
    rules.collectFirstSome(_.evaluate(in))

}
