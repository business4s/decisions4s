package decisions4s

import decisions4s.exprs.UnaryTest
import decisions4s.internal.HKDUtils
import decisions4s.internal.HKDUtils.Const
import shapeless3.deriving.~>

class Rule[Input[_[_]]: HKD, Output[_[_]]: HKD](
    val matching: EvaluationContext[Input] ?=> Input[UnaryTest],
    val output: EvaluationContext[Input] ?=> Output[OutputValue],
    val annotation: Option[String] = None,
) {

  def evaluateOutput()(using EvaluationContext[Input]): Output[Value] = {
    val evaluate: OutputValue ~> Value = [t] => expr => expr.evaluate
    output.mapK(evaluate)
  }

  def evaluate(in: Input[Value])(using EvaluationContext[Input]): RuleResult[Input, Output] = {
    val evaluated: Input[Const[Boolean]]  = HKD.map2(matching, in)([t] => (expr, value) => expr.evaluate(value))
    val matches                           = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evalResult: Option[Output[Value]] = Option.when(matches)(evaluateOutput())
    RuleResult(evaluated, evalResult)
  }

  def render(): (Input[Description], Output[Description]) = {
    given EvaluationContext[Input] = EvaluationContext.stub
    (
      matching.mapK([t] => expr => expr.renderExpression),
      output.mapK([t] => expr => expr.renderExpression),
    )
  }

}

object Rule {
  def default[Input[_[_]]: HKD, Output[_[_]]: HKD](value: Output[OutputValue]): Rule[Input, Output] = {
    Rule(
      matching = HKD[Input].pure[UnaryTest]([t] => () => it.catchAll[t]),
      output = value,
    )
  }
}
