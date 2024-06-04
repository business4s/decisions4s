package decisions4s.internal

import decisions4s.{DecisionTable, EvalDiagnostics, HKD, Rule, Value}

sealed trait UniqueEvalResult[Input[_[_]], Output[_[_]]] {
  def diagnostics: EvalDiagnostics[Input, Output]
  def toOption: Option[Output[Value]] = this match {
    case UniqueEvalResult.NotUnique(rawResults)       => None
    case UniqueEvalResult.NoHit(rawResults)           => None
    case UniqueEvalResult.Success(rawResults, output) => Some(output)
  }
}
object UniqueEvalResult                                  {
  case class NotUnique[Input[_[_]], Output[_[_]]](diagnostics: EvalDiagnostics[Input, Output]) extends UniqueEvalResult[Input, Output]
  case class NoHit[Input[_[_]], Output[_[_]]](diagnostics: EvalDiagnostics[Input, Output])     extends UniqueEvalResult[Input, Output]
  case class Success[Input[_[_]], Output[_[_]]](diagnostics: EvalDiagnostics[Input, Output], output: Output[Value])
      extends UniqueEvalResult[Input, Output]
}

sealed trait AnyEvalResult[Input[_[_]], Output[_[_]]] {
  def rawResults: List[Rule.Result[Input, Output]]
}
object AnyEvalResult                                  {
  case class NotUnique[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]]) extends AnyEvalResult[Input, Output]
  case class NoHit[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]])     extends AnyEvalResult[Input, Output]
  case class Success[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: Output[Value])
      extends AnyEvalResult[Input, Output]
}

case class FirstEvalResult[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: Option[Output[Value]])

case class CollectEvalResult[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: List[Output[Value]])

case class CollectSumEvalResult[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: Option[Output[Value]])

case class CollectBoundEvalResult[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: Option[Output[Value]])

case class CollectCountEvalResult[Input[_[_]], Output[_[_]]](rawResults: List[Rule.Result[Input, Output]], output: Int)

class EvaluationResultTransformer[Input[_[_]]: HKD, Output[_[_]]](
    rawResults: Seq[() => Rule.Result[Input, Output]],
    table: DecisionTable[Input, Output, _],
    input: Input[Value],
) {

  /** Return result if exactly one rule was satisfied Could be made lazy (stop evaluation on second unique result) if needed
    * @return
    */
  def single(): UniqueEvalResult[Input, Output] = {
    val raw              = rawResults.map(_.apply()).toList
    val diagnostics      = EvalDiagnostics(raw, table, input)
    val satisfiedResults = raw.flatMap(_.evaluationResult.toOption)
    satisfiedResults match {
      case Nil                 => UniqueEvalResult.NoHit(diagnostics)     // No satisfied results found
      case singleResult :: Nil => UniqueEvalResult.Success(diagnostics, singleResult)
      case _                   => UniqueEvalResult.NotUnique(diagnostics) // More than one unique result
    }
  }

  /** Returns result if there is unique result produced by all satisfied rules.
    */
  def anyUnique(): AnyEvalResult[Input, Output] = {
    val raw              = rawResults.map(_.apply()).toList
    val satisfiedResults = raw.flatMap(_.evaluationResult.toOption)

    satisfiedResults match {
      case Nil                 => AnyEvalResult.NoHit(raw) // No satisfied results found
      case singleResult :: Nil => AnyEvalResult.Success(raw, singleResult)
      case more                =>
        val unique = more.toSet
        if (unique.size == 1) AnyEvalResult.Success(raw, more.head)
        else AnyEvalResult.NotUnique(raw) // More than one unique result
    }
  }

  def first(): FirstEvalResult[Input, Output] = {
    val (results, firstSatisfied) = rawResults.foldLeft((List.empty[Rule.Result[Input, Output]], Option.empty[Output[Value]])) {
      case ((acc, None), getResult)         =>
        getResult() match {
          case result @ Rule.Result(_, Rule.EvaluationResult.Satisfied(values)) => (acc :+ result, Some(values))
          case result                                                           => (acc :+ result, None)
        }
      case ((acc, found @ Some(_)), result) => (acc, found)
    }
    FirstEvalResult(results, firstSatisfied)
  }

  def collect(): CollectEvalResult[Input, Output] = {
    val raw       = rawResults.map(_.apply()).toList
    val satisfied = raw.flatMap(_.evaluationResult.toOption)
    CollectEvalResult(raw, satisfied)
  }

  def collectSum(merge: (Output[Value], Output[Value]) => Output[Value]): CollectSumEvalResult[Input, Output] = {
    val raw = rawResults.map(_.apply()).toList
    val sum = raw.flatMap(_.evaluationResult.toOption).reduceOption(merge)
    CollectSumEvalResult(raw, sum)
  }

  def collectMin()(using Ordering[Output[Value]]): CollectBoundEvalResult[Input, Output] = {
    val raw = rawResults.map(_.apply()).toList
    val min = raw.flatMap(_.evaluationResult.toOption).minOption
    CollectBoundEvalResult(raw, min)
  }

  def collectCount(): CollectCountEvalResult[Input, Output] = {
    val raw   = rawResults.map(_.apply()).toList
    val count = raw.flatMap(_.evaluationResult.toOption).size
    CollectCountEvalResult(raw, count)
  }

}
