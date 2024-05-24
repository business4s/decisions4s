package decisions4s

case class DecisionTable[Input[_[_]], Output[_[_]]](
    rules: List[Rule[Input, Output]],
    inputNames: Input[Name],
    outputNames: Output[Name],
    name: String,
) {

  def evaluate(in: Input[Value]): Option[Output[Value]] =
    rules.flatMap(_.evaluate(in)).headOption

}
