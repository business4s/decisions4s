package decisions4s

import decisions4s.exprs.Variable
import decisions4s.internal.EvaluationResultTransformer

import scala.util.chaining.scalaUtilChainingOps

case class DecisionTable[Input[_[_]], Output[_[_]], HitPolicy <: DecisionTable.HitPolicy](
    rules: List[Rule[Input, Output]],
    name: String,
    hitPolicy: HitPolicy,
)(using val inputHKD: HKD[Input], val outputHKD: HKD[Output]) {

  private def evaluateRaw(in: Input[Value]): Seq[() => RuleResult[Input, Output]] = {
    given EvaluationContext[Input] = new EvaluationContext[Input] {
      override val wholeInput: Input[Expr] = HKD[Input].map2(in, HKD[Input].meta)([t] => (value, meta) => Variable[t](meta.name, value))
    }
    rules.map(r => () => r.evaluate(in))
  }

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
    def evaluateSingle(in: Input[Value]): EvalResult.Single[Input, Output] =
      transformer(dt, in).single()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Distinct]) {
    def evaluateDistinct(in: Input[Value]): EvalResult.Distinct[Input, Output] =
      transformer(dt, in).distinct()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.First]) {
    def evaluateFirst(in: Input[Value]): EvalResult.First[Input, Output] =
      transformer(dt, in).first()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.Collect]) {
    def evaluateCollect(in: Input[Value]): EvalResult.Collect[Input, Output] =
      transformer(dt, in).collect()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectSum]) {
    def evaluateSum(in: Input[Value])(merge: (Output[Value], Output[Value]) => Output[Value]): EvalResult.Sum[Input, Output] =
      transformer(dt, in).collectSum(merge)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMin]) {
    def evaluateMin(in: Input[Value])(using Ordering[Output[Value]]): EvalResult.Min[Input, Output] =
      transformer(dt, in).collectMin()
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectMax]) {
    def evaluateMax(in: Input[Value])(using ord: Ordering[Output[Value]]): EvalResult.Max[Input, Output] =
      transformer(dt, in).collectMin()(using ord.reverse)
  }
  extension [Input[_[_]], Output[_[_]]](dt: DecisionTable[Input, Output, HitPolicy.CollectCount]) {
    def evaluateCount(in: Input[Value]): EvalResult.Count[Input, Output] =
      transformer(dt, in).collectCount()
  }

  private def transformer[Input[_[_]], Output[_[_]]](
      dt: DecisionTable[Input, Output, ?],
      in: Input[Value],
  ): EvaluationResultTransformer[Input, Output] = dt.evaluateRaw(in).pipe(EvaluationResultTransformer(_, dt, in))

}
