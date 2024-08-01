package decisions4s.cats.effect

import _root_.cats.effect.std.Dispatcher
import _root_.cats.{Applicative, Monad}
import _root_.cats.effect.{Async, Concurrent, Ref, Resource}
import _root_.cats.implicits.{catsSyntaxApplicativeId, none, toFunctorOps}
import cats.syntax.all.{toTraverseOps, given}
import decisions4s.*
import decisions4s.DecisionTable.HitPolicy
import decisions4s.exprs.UnaryTest
import decisions4s.internal.HKDUtils
import shapeless3.deriving.Const

import scala.util.chaining.scalaUtilChainingOps

class MemoizingEvaluator[Input[_[_]], Output[_[_]], F[_]: Concurrent: Async](val dt: DecisionTable[Input, Output, HitPolicy.First]) {

  import dt.given

  def evaluateFirst(in: Input[F]): F[EvalResult.First[Input, Output]] = {
    for {
      memoized             <- memoizeInput(in)
      (updating, refs)     <- collectValues(memoized)
      result               <- evaluateRules(updating)
      (ruleResults, output) = result
      evaluatedInputs      <- collectEvaluatedFields(refs)
    } yield EvalResult(dt, evaluatedInputs, ruleResults, output)
  }

  private type RulesResult = (List[RuleResult[Input, Output]], Option[Output[Value]])
  private def evaluateRules(in: Input[F]): F[RulesResult] = {
    buildContext(in).use(ctx =>
      dt.rules.foldLeftM[F, RulesResult]((List(), None)) {
        case ((acc, None), rule) =>
          val resultF = evaluateRule(rule, in)(using ctx)
          resultF.map(result => (acc :+ result, result.evaluationResult))
        case (acc, _)            => acc.pure[F]
      },
    )
  }

  // memoizes all the fields and reconstructs the input
  private def memoizeInput(input: Input[F]): F[Input[F]] = {
    type Memoized[T] = F[F[T]]
    val memoize: F ~> Memoized = [t] => (f: F[t]) => Concurrent[F].memoize(f)
    val memoized: F[Input[F]]  = input.mapK(memoize).pipe(sequence[Input, F, F])
    memoized
  }

  // collectValues
  type OptionRef[T] = Ref[F, Option[T]]
  private def collectValues(input: Input[F]): F[(Input[F], Input[OptionRef])] = {
    val refsF: Input[[t] =>> F[OptionRef[t]]] = dt.inputHKD.pure([t] => () => Ref[F].of(none[t]))
    for {
      refs    <- sequence[Input, F, OptionRef](refsF)
      updating = HKD.map2(input, refs)([t] => (fValue, ref) => Monad[F].flatTap(fValue)(value => ref.set(Some(value))))
    } yield (updating, refs)
  }

  private def evaluateRule(rule: Rule[Input, Output], input: Input[F])(using EvaluationContext[Input]): F[RuleResult[Input, Output]] = {
    type FBool[T] = F[Boolean]
    val evaluatedF: Input[FBool] = HKD.map2(rule.matching, input)(
      [t] =>
        (expr, fValue) =>
          {
            (expr: UnaryTest[t]) match {
              case UnaryTest.CatchAll => true.pure[F]
              case _                  => fValue.map(expr.evaluate)
            }
          },
    )
    val sequenced                = sequence[Input, F, Const[Boolean]](evaluatedF)
    val result                   = for {
      evaluated <- sequenced
      matches    = HKDUtils.collectFields(evaluated).foldLeft(true)(_ && _)
      evalResult = Option.when(matches)(rule.evaluateOutput())
    } yield RuleResult(evaluated, evalResult)
    result
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

  private def collectEvaluatedFields(refs: Input[OptionRef]): F[Input[Option]] = {
    sequence(refs.mapK([t] => ref => ref.get))
  }

  private def buildContext(input: Input[F]): Resource[F, EvaluationContext[Input]] = {
    Dispatcher
      .sequential[F]
      .map(dispatcher => {
        val variables: Input[[t] =>> Expr[t]] =
          HKD.map2(input, HKD.typedNames[Input])([t] => (fValue, name) => FVariable[t](name, fValue, dispatcher))
        new EvaluationContext[Input] {
          override def wholeInput: Input[Expr] = variables
        }
      })
  }

  case class FVariable[T](name: String, value: F[T], dispatcher: Dispatcher[F]) extends Expr[T] {
    override def evaluate: T = dispatcher.unsafeRunSync(value)

    override def renderExpression: String = name
  }

}
