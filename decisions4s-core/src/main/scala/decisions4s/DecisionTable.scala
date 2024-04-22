package decisions4s

import cats.syntax.all.*

case class DecisionTable[Input[_[_]], Output[_[_]]](rules: List[Rule[Input, Output]]) {

  def evaluate(in: Input[Value]): Option[Output[Value]] = 
    rules.collectFirstSome(_.evaluate(in))

}
