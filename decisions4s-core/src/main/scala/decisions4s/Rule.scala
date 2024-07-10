package decisions4s

import decisions4s.exprs.{Variable, VariableStub}
import decisions4s.internal.HKDUtils
import shapeless3.deriving.{Const, ~>}

case class Rule[Input[_[_]]: HKD, Output[_[_]]: HKD](
    matching: Input[MatchingExpr[Input]],
    output: Output[OutputValue],
    annotation: Option[String] = None,
) {

  def evaluateOutput(): Output[Value] = {
    val evaluate: ValueExpr ~> Value = [t] => (fa: ValueExpr[t]) => fa.evaluate(())
    output.mapK(evaluate)
  }

  def evaluate(in: Input[Value]): Rule.Result[Input, Output] = {
    type Bool[T] = Boolean
    given EvaluationContext[Input]        = new EvaluationContext[Input] {
      override val wholeInput: Input[ValueExpr] = HKD.map2(in, HKD.typedNames[Input])([t] => (value, name) => Variable[t](name, value))
    }
    val evaluated: Input[Bool]            = HKD.map2(matching, in)([t] => (expr, value) => expr.evaluate(value))
    val matches                           = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
    val evalResult: Option[Output[Value]] = Option.when(matches)(evaluateOutput())
    Rule.Result(evaluated, evalResult)
  }

  def render(): (Input[Description], Output[Description]) = {
    given EvaluationContext[Input]        = new EvaluationContext[Input] {
      override val wholeInput: Input[ValueExpr] = HKD.typedNames[Input].mapK([t] => name => VariableStub[t](name))
    }
    (
      matching.mapK([t] => expr => expr.renderExpression),
      output.mapK([t] => expr => expr.renderExpression)
    )
  }

}

object Rule {
  def default[Input[_[_]]: HKD, Output[_[_]]: HKD](value: Output[OutputValue]): Rule[Input, Output] = {
    Rule(
      matching = HKD[Input].pure[MatchingExpr[Input]]([t] => () => it.catchAll[t]),
      output = value,
    )
  }

  case class Result[Input[_[_]], Output[_[_]]](details: Input[Const[Boolean]], evaluationResult: Option[Output[Value]])
}
