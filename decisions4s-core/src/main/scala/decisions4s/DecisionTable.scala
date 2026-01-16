package decisions4s

import decisions4s.DecisionTable.transformer
import decisions4s.exprs.Variable
import decisions4s.internal.{EvaluationResultTransformer, ~>}

import scala.util.chaining.scalaUtilChainingOps

case class DecisionTable[Input[_[_]], Output[_[_]]](
    rules: Seq[Rule[Input, Output]],
    name: String,
)(using val inputHKD: HKD[Input], val outputHKD: HKD[Output]) {

  def evaluateRaw(in: Input[Value]): LazyList[RuleResult[Input, Output]] = {
    given EvaluationContext[Input] = EvaluationContext.fromInput(in)
    rules.to(LazyList).map(r => r.evaluate(in))
  }

  def evaluateSingle(in: Input[Value]): EvalResult.Single[Input, Output] = {
    val raw              = evaluateRaw(in).toList
    val satisfiedResults = raw.flatMap(_.evaluationResult)
    satisfiedResults match {
      case Nil                 => result(in, raw, Right(None))
      case singleResult :: Nil => result(in, raw, Right(Some(singleResult)))
      case _                   => result(in, raw, Left("not-single")) // More than one unique result
    }
  }

  def evaluateDistinct(in: Input[Value]): EvalResult.Distinct[Input, Output] =
    transformer(this, in).distinct()

  def evaluateFirst(in: Input[Value]): EvalResult.First[Input, Output] =
    transformer(this, in).first()

  def evaluateCollect(in: Input[Value]): EvalResult.Collect[Input, Output] =
    transformer(this, in).collect()

  def evaluateSum(in: Input[Value])(merge: (Output[Value], Output[Value]) => Output[Value]): EvalResult.Sum[Input, Output] =
    transformer(this, in).collectSum(merge)

  def evaluateMin(in: Input[Value])(using Ordering[Output[Value]]): EvalResult.Min[Input, Output] =
    transformer(this, in).collectMin()

  def evaluateMax(in: Input[Value])(using ord: Ordering[Output[Value]]): EvalResult.Max[Input, Output] =
    transformer(this, in).collectMin()(using ord.reverse)

  def evaluateCount(in: Input[Value]): EvalResult.Count[Input, Output] =
    transformer(this, in).collectCount()

  private def result[T](input: Input[Value], rawResults: List[RuleResult[Input, Output]], output: T): EvalResult[Input, Output, T] = {
    val toSome: Value ~> Option = [t] => (v: t) => Some(v): Option[t]
    EvalResult(this, input.mapK(toSome), rawResults, output)
  }
}

object DecisionTable {

  private def transformer[Input[_[_]], Output[_[_]]](
      dt: DecisionTable[Input, Output],
      in: Input[Value],
  ): EvaluationResultTransformer[Input, Output] = dt.evaluateRaw(in).map(() => _).pipe(EvaluationResultTransformer(_, dt, in))

}
