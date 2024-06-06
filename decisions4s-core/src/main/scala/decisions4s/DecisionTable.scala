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
    type Unique = Unique.type
    case object Unique extends HitPolicy
    type Any = Any.type
    case object Any extends HitPolicy
    type First = First.type
    case object First extends HitPolicy
    // Rule order is not supported as it brings no benefit over Collect. There is no reason to use different order than rule order.
    //    type RuleOrder = RuleOrder.type
    //    case object RuleOrder extends HitPolicy
    type Collect = Collect.type
    case object Collect extends HitPolicy
    type CollectSum = CollectSum.type
    case object CollectSum extends HitPolicy
    type CollectMin = CollectMin.type
    case object CollectMin extends HitPolicy
    type CollectMax = CollectMax.type
    case object CollectMax extends HitPolicy
    type CollectCount = CollectCount.type
    case object CollectCount extends HitPolicy
  }

  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Unique]) {
    def evaluateUnique(in: Input[Value]): UniqueEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).single()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Any]) {
    def evaluateAny(in: Input[Value]): AnyEvalResult[Input, Output] =
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
    def evaluateCollectSum(in: Input[Value])(merge: (Output[Value], Output[Value]) => Output[Value]): CollectSumEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectSum(merge)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMin]) {
    def evaluateCollectMin(in: Input[Value])(using Ordering[Output[Value]]): CollectBoundEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectMin()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMax]) {
    def evaluateCollectMax(in: Input[Value])(using ord: Ordering[Output[Value]]): CollectBoundEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectMin()(using ord.reverse)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectCount]) {
    def evaluateCollectCount(in: Input[Value]): CollectCountEvalResult[Input, Output] =
      dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_)).collectCount()
  }

}
