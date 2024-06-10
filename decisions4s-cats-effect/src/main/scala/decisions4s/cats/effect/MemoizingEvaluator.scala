package decisions4s.cats.effect

import cats.Applicative
import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import cats.syntax.all.{toTraverseOps, given}
import decisions4s.DecisionTable.HitPolicy
import decisions4s.exprs.UnaryTest
import decisions4s.internal.HKDUtils
import decisions4s.*
import shapeless3.deriving.Const

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

private class MemoizingEvaluator[Input[_[_]]: HKD, Output[_[_]]: HKD, F[_]: Concurrent](val dt: DecisionTable[Input, Output, HitPolicy.First]) {

  def evaluateFirst(in: Input[F]): F[EvalResult.First[Input, Output]] = {
    for {
      memoized                              <- memoizeInput(in)
      result                                <- evaluateRules(memoized)
      (ruleResults, output, evaluatedFields) = result
      evaluatedInputs                       <- collectEvaluatedFields(evaluatedFields, memoized)
    } yield EvalResult(dt, evaluatedInputs, ruleResults, output)
  }

  type EvaluatedFields     = Set[FieldIdx]
  private type RulesResult = (List[Rule.Result[Input, Output]], Option[Output[Value]], EvaluatedFields)
  private def evaluateRules(in: Input[F]): F[RulesResult] = {
    dt.rules.foldLeftM[F, RulesResult]((List(), None, Set())) {
      case ((acc, None, evaluatedFieldsAcc), rule) =>
        val (resultF, evaluatedFields) = evaluateRule(rule, in)
        resultF.map(result => (acc :+ result, result.evaluationResult, evaluatedFieldsAcc ++ evaluatedFields))
      case (acc, _)                                => acc.pure[F]
    }
  }

  // memoizes all the fields and reconstructs the input
  private def memoizeInput(input: Input[F]): F[Input[F]] = {
    type Memoized[T] = F[F[T]]
    val memoize: F ~> Memoized = [t] => (f: F[t]) => Concurrent[F].memoize(f)
    val memoized: F[Input[F]]  = input.mapK(memoize).pipe(sequence[Input, F, F])
    memoized
  }

  type FieldIdx = Int
  private def evaluateRule(rule: Rule[Input, Output], input: Input[F]): (F[Rule.Result[Input, Output]], Set[FieldIdx]) = {
    type FBool[T] = F[Boolean]
    type Tup[T]   = Tuple2K[MatchingExpr, F][T]
    val evaluatedFields: mutable.Set[FieldIdx] = mutable.Set()
    var idx: FieldIdx                          = 0;
    val evaluateMatch: Tup ~> FBool            = [t] =>
      (tuple: Tup[t]) => {
        val expr: MatchingExpr[t] = tuple._1
        val fValue: F[t]          = tuple._2
        val result: F[Boolean]    = expr match {
          case UnaryTest.CatchAll => true.pure[F]
          case _                  => {
            evaluatedFields.add(idx)
            fValue.map(expr.evaluate)
          }
        }
        idx += 1;
        result
    }
    val evaluatedF: Input[FBool]               = rule.matching.productK(input).mapK(evaluateMatch)
    val sequenced                              = sequence[Input, F, Const[Boolean]](evaluatedF)
    val result                                 = for {
      evaluated <- sequenced
      matches    = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
      evalResult = Option.when(matches)(rule.evaluateOutput())
    } yield Rule.Result(evaluated, evalResult)
    result -> evaluatedFields.toSet
  }

  // Weird implementation of usual sequence/traverse. Collects all the fields, sequences them and reconstruct the case class
  private def sequence[CaseClass[_[_]]: HKD, G[_]: Applicative, H[_]](instance: CaseClass[[t] =>> G[H[t]]]): G[CaseClass[H]] = {
    val collected: List[G[H[Any]]] = HKDUtils.collectFields[CaseClass, G[H[Any]]](instance.asInstanceOf[CaseClass[Const[G[H[Any]]]]])
    collected.sequence
      .map(values => {
        var idx                  = 0
        val result: CaseClass[H] = summon[HKD[CaseClass]].pure[H](
          [t] =>
            () =>
              {
                val value = values(idx)
                idx += 1
                value.asInstanceOf[H[t]]
              },
        )
        result
      })
  }

  private def collectEvaluatedFields(idxs: EvaluatedFields, in: Input[F]): F[Input[Option]] = {
    var idx: FieldIdx = -1
    type FOption[T] = F[Option[T]]
    val keepEvaluated: F ~> FOption = [t] =>
      (fValue: F[t]) => {
        idx += 1
        if (idxs.contains(idx)) fValue.map(_.some)
        else None.pure[F]
    }
    val onlyEvaluated               = in.mapK(keepEvaluated)
    sequence(onlyEvaluated)
  }

}
