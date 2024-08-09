package decisions4s

import decisions4s.exprs.Variable
import decisions4s.internal.EvaluationResultTransformer

import scala.util.chaining.scalaUtilChainingOps

case class DecisionTable[Input[_[_]], Output[_[_]], HitPolicy <: decisions4s.HitPolicy](
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
