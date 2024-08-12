package decisions4s

import decisions4s.internal.Const

case class RuleResult[Input[_[_]], Output[_[_]]](
    details: Input[Const[Boolean]],
    evaluationResult: Option[Output[Value]],
)
