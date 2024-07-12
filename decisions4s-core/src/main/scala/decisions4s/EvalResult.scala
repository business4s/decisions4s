package decisions4s

import decisions4s.internal.DiagnosticsPrinter

/** Results of evaluating a decision. Contains all the data required to analyze why the decision was taken.
  *
  * @param table
  *   the decision table that was evaluated
  * @param input
  *   the input values used during evaluation. `Option` because not all provided values might have been evaluated and used
  * @param results
  *   results from particular rules
  * @param output
  *   value produced by the table as a whole based on hit policy
  */
case class EvalResult[In[_[_]], Out[_[_]], +Output](
    table: DecisionTable[In, Out, ?],
    input: In[Option],
    results: List[RuleResult[In, Out]],
    output: Output,
) {

  def makeDiagnosticsString: String = DiagnosticsPrinter(this).print

}

object EvalResult {
  type Single[I[_[_]], O[_[_]]]   = EvalResult[I, O, Either["not-single", Option[O[Value]]]]
  type Distinct[I[_[_]], O[_[_]]] = EvalResult[I, O, Either["not-distinct", Option[O[Value]]]]
  type First[I[_[_]], O[_[_]]]    = EvalResult[I, O, Option[O[Value]]]
  type Collect[I[_[_]], O[_[_]]]  = EvalResult[I, O, List[O[Value]]]
  type Sum[I[_[_]], O[_[_]]]      = EvalResult[I, O, Option[O[Value]]]
  type Min[I[_[_]], O[_[_]]]      = EvalResult[I, O, Option[O[Value]]]
  type Max[I[_[_]], O[_[_]]]      = EvalResult[I, O, Option[O[Value]]]
  type Count[I[_[_]], O[_[_]]]    = EvalResult[I, O, Int]
}
