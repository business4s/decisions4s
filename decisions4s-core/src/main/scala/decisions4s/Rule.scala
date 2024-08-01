package decisions4s

import decisions4s.exprs.VariableStub
import decisions4s.internal.HKDUtils
import shapeless3.deriving.~>

case class Rule[Input[_[_]]: HKD, Output[_[_]]: HKD](
    matching: Input[MatchingExpr[Input]],
    output: Output[OutputExpr[Input]],
    annotation: Option[String] = None,
) {

  def evaluateOutput()(using EvaluationContext[Input]): Output[Value] = {
    val evaluate: OutputExpr[Input] ~> Value = [t] => expr => expr.evaluate
    output.mapK(evaluate)
  }

  def evaluate(in: Input[Value])(using EvaluationContext[Input]): RuleResult[Input, Output] = {
    type Bool[T] = Boolean
    val evaluated: Input[Bool]            = HKD.map2(matching, in)([t] => (expr, value) => expr.evaluate(value))
    val matches                           = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evalResult: Option[Output[Value]] = Option.when(matches)(evaluateOutput())
    RuleResult(evaluated, evalResult)
  }

  def render(): (Input[Description], Output[Description]) = {
    given EvaluationContext[Input] = new EvaluationContext[Input] {
      override val wholeInput: Input[Expr] = HKD.typedNames[Input].mapK([t] => name => VariableStub[t](name))
    }
    (
      matching.mapK([t] => expr => expr.renderExpression),
      output.mapK([t] => expr => expr.renderExpression),
    )
  }

}

object Rule {
  def default[Input[_[_]]: HKD, Output[_[_]]: HKD](value: Output[OutputExpr[Input]]): Rule[Input, Output] = {
    Rule(
      matching = HKD[Input].pure[MatchingExpr[Input]]([t] => () => it.catchAll[t]),
      output = value.mapK([t] => v => v),
    )
  }
}
