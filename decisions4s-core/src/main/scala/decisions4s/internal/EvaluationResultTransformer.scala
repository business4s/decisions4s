package decisions4s.internal

import decisions4s.*

class EvaluationResultTransformer[Input[_[_]], Output[_[_]]](
    rawResults: Seq[() => RuleResult[Input, Output]],
    table: DecisionTable[Input, Output, ?],
    input: Input[Value],
) {

  /** Return a result if exactly one rule was satisfied. Could be made lazy (stop evaluation on the second unique result) if needed.
    */
  def single(): EvalResult.Single[Input, Output] = {
    val raw              = rawResults.map(_.apply()).toList
    val satisfiedResults = raw.flatMap(_.evaluationResult)
    satisfiedResults match {
      case Nil                 => result(raw, Right(None))
      case singleResult :: Nil => result(raw, Right(Some(singleResult)))
      case _                   => result(raw, Left("not-single")) // More than one unique result
    }
  }

  /** Returns result if there is a unique result produced by all satisfied rules. Equality is governed by the supplied [[Equiv]] instance, making the
    * comparison rule explicit at the call site (e.g. to opt into bit-exact Double comparison via `doubleToRawLongBits`).
    */
  def distinct()(using eq: Equiv[Output[Value]]): EvalResult.Distinct[Input, Output] = {
    val raw              = rawResults.map(_.apply()).toList
    val satisfiedResults = raw.flatMap(_.evaluationResult)
    satisfiedResults match {
      case Nil          => result(raw, Right(None))
      case head :: tail =>
        if (tail.forall(eq.equiv(_, head))) result(raw, Right(Some(head)))
        else result(raw, Left("not-distinct"))
    }
  }

  def first(): EvalResult.First[Input, Output] = {
    val (results, firstSatisfied) = rawResults.foldLeft((List.empty[RuleResult[Input, Output]], Option.empty[Output[Value]])) {
      case ((acc, None), getResult)    =>
        val result = getResult()
        (acc :+ result, result.evaluationResult)
      case ((acc, found @ Some(_)), _) => (acc, found)
    }
    result(results, firstSatisfied)
  }

  def collect(): EvalResult.Collect[Input, Output] = {
    val raw       = rawResults.map(_.apply()).toList
    val satisfied = raw.flatMap(_.evaluationResult)
    result(raw, satisfied)
  }

  def collectSum(merge: (Output[Value], Output[Value]) => Output[Value]): EvalResult.Sum[Input, Output] = {
    val raw = rawResults.map(_.apply()).toList
    val sum = raw.flatMap(_.evaluationResult).reduceOption(merge)
    result(raw, sum)
  }

  def collectMin()(using Ordering[Output[Value]]): EvalResult.Min[Input, Output] = {
    val raw = rawResults.map(_.apply()).toList
    val min = raw.flatMap(_.evaluationResult).minOption
    result(raw, min)
  }

  def collectCount(): EvalResult.Count[Input, Output] = {
    val raw   = rawResults.map(_.apply()).toList
    val count = raw.flatMap(_.evaluationResult).size
    result(raw, count)
  }

  private def result[T](rawResults: List[RuleResult[Input, Output]], output: T): EvalResult[Input, Output, T] = {
    val toSome: Value ~> Option = [t] => (v: t) => Some(v): Option[t]
    import table.given
    EvalResult(table, input.mapK(toSome), rawResults, output)
  }

}
