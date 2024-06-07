package decisions4s

import decisions4s.internal.{
  AnyEvalResult,
  CollectBoundEvalResult,
  CollectCountEvalResult,
  CollectEvalResult,
  CollectSumEvalResult,
  EvaluationResultTransformer,
  FirstEvalResult,
  UniqueEvalResult,
}
import shapeless3.deriving.Const

import scala.util.chaining.scalaUtilChainingOps

case class DecisionTable[Input[_[_]]: HKD, Output[_[_]]: HKD, HitPolicy <: DecisionTable.HitPolicy](
    rules: List[Rule[Input, Output]],
    name: String,
    hitPolicy: HitPolicy,
)(using val inputHKD: HKD[Input], val outputHKD: HKD[Output]) {

  private def evaluateRaw(in: Input[Value]): Seq[() => Rule.Result[Input, Output]] =
    rules.map(r => () => r.evaluate(in))

}

object DecisionTable {

  sealed trait HitPolicy
  object HitPolicy {
    // Only a single rule is allowed to be satisfied. Matches to `Unique` in DNM.
    type Single = Single.type
    case object Single extends HitPolicy

    // Many rules can be satisfied, but they need to produce the same results. Matches to `Any` in DNM.
    type Distinct = Distinct.type
    case object Distinct extends HitPolicy

    // First satisfied rule produces the output
    type First = First.type
    case object First extends HitPolicy

    // Rule order is not supported as it brings no benefit over Collect. There is no reason to use different order than rule order.
    // type RuleOrder = RuleOrder.type
    // case object RuleOrder extends HitPolicy

    // Outputs are collected
    type Collect = Collect.type
    case object Collect extends HitPolicy

    // Outputs are summed together
    type CollectSum = CollectSum.type
    case object CollectSum extends HitPolicy

    // Minimum output is returned
    type CollectMin = CollectMin.type
    case object CollectMin extends HitPolicy

    // Maximum output is returned
    type CollectMax = CollectMax.type
    case object CollectMax extends HitPolicy

    // Number of satisfied rules is returned
    type CollectCount = CollectCount.type
    case object CollectCount extends HitPolicy
  }

  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Single]) {
    def evaluateSingle(in: Input[Value]): UniqueEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).single()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Distinct]) {
    def evaluateDistinct(in: Input[Value]): AnyEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).anyUnique()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.First]) {
    def evaluateFirst(in: Input[Value]): FirstEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).first()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Collect]) {
    def evaluateCollect(in: Input[Value]): CollectEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collect()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectSum]) {
    def evaluateSum(in: Input[Value])(merge: (Output[Value], Output[Value]) => Output[Value]): CollectSumEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectSum(merge)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMin]) {
    def evaluateMin(in: Input[Value])(using Ordering[Output[Value]]): CollectBoundEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectMin()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMax]) {
    def evaluateMax(in: Input[Value])(using ord: Ordering[Output[Value]]): CollectBoundEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectMin()(using ord.reverse)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectCount]) {
    def evaluateCount(in: Input[Value]): CollectCountEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectCount()
  }

}
