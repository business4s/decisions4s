package decisions4s.cats.effect

import cats.Applicative
import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import cats.syntax.all.toTraverseOps
import decisions4s.{DecisionTable, HKD, MatchingExpr, Rule, Tuple2K, Value, ~>}
import decisions4s.DecisionTable.HitPolicy
import decisions4s.Rule.EvaluationResult
import decisions4s.exprs.UnaryTest
import decisions4s.internal.{FirstEvalResult, HKDUtils}
import shapeless3.deriving.Const
import cats.syntax.all.given

import scala.util.chaining.scalaUtilChainingOps

private class MemoizingEvaluator[Input[_[_]]: HKD, Output[_[_]]: HKD, F[_]: Concurrent](val dt: DecisionTable[Input, Output, HitPolicy.First]) {

  def evaluateFirst(in: Input[F]): F[FirstEvalResult[Input, Output]] = {
    for {
      memoized <- memoizeInput(in)
      result   <- evaluateRules(memoized)
    } yield FirstEvalResult[Input, Output](result._1, result._2)
  }

  private type RulesResult = (List[Rule.Result[Input, Output]], Option[Output[Value]])
  private def evaluateRules(in: Input[F]): F[RulesResult] = {
    dt.rules.foldLeftM[F, RulesResult]((List(), None)) {
      case ((acc, None), rule)              =>
        evaluateRule(rule, in).map {
          case result @ Rule.Result(_, Rule.EvaluationResult.Satisfied(output)) => (acc :+ result, Some(output))
          case result                                                           => (acc :+ result, None)
        }
      case ((acc, found @ Some(_)), result) => (acc, found).pure[F]
    }
  }

  // memoizes all the fields and reconstructs the input
  private def memoizeInput(input: Input[F]): F[Input[F]] = {
    type Memoized[T] = F[F[T]]
    val memoize: F ~> Memoized = [t] => (f: F[t]) => Concurrent[F].memoize(f)
    val memoized: F[Input[F]]  = input.mapK(memoize).pipe(sequence[Input, F, F])
    memoized
  }

  private def evaluateRule(rule: Rule[Input, Output], input: Input[F]): F[Rule.Result[Input, Output]] = {
    type FBool[T] = F[Boolean]
    type Tup[T]   = Tuple2K[MatchingExpr, F][T]
    val evaluateMatch: Tup ~> FBool = [t] =>
      (tuple: Tup[t]) => {
        val expr: MatchingExpr[t] = tuple._1
        val fValue: F[t]          = tuple._2
        val result: F[Boolean]    = expr match {
          case UnaryTest.CatchAll => true.pure[F]
          case _                  => fValue.map(expr.evaluate)
        }
        result
    }
    val evaluatedF: Input[FBool]    = rule.matching.productK(input).mapK(evaluateMatch)
    val sequenced                   = sequence[Input, F, Const[Boolean]](evaluatedF)
    for {
      evaluated <- sequenced
      matches    = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
      evalResult = if (matches) EvaluationResult.Satisfied(rule.evaluateOutput())
                   else EvaluationResult.NotSatisfied()
    } yield Rule.Result(evaluated, evalResult)
  }

  // weird implementation of usual sequence/traverse. Collects all the fields, sequences them and reconstruct the case class
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

}
