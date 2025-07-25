package decisions4s.cats.effect

import _root_.cats.effect.std.Dispatcher
import _root_.cats.effect.{Async, Concurrent, Ref, Resource}
import _root_.cats.implicits.{catsSyntaxApplicativeId, none}
import _root_.cats.{Applicative, Monad}
import cats.syntax.all.{toTraverseOps, given}
import decisions4s.*
import decisions4s.exprs.UnaryTest
import decisions4s.internal.{HKDUtils, ~>}
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
    val memoized: F[Input[F]]  = input.mapK1(memoize).pipe(sequence[Input, F, F])
    memoized
  }

  type OptionRef[T] = Ref[F, Option[T]]

  // each field, when evaluated, will set the value in the corresponding Ref
  private def collectValues(input: Input[F]): F[(Input[F], Input[OptionRef])] = {
    val refsF: Input[[t] =>> F[OptionRef[t]]] = dt.inputHKD.pure([t] => () => Ref[F].of(none[t]))
    for {
      refs    <- sequence[Input, F, OptionRef](refsF)
      updating = HKD.map2(input, refs)([t] => (fValue, ref) => Monad[F].flatTap(fValue)(value => ref.set(Some(value))))
    } yield (updating, refs)
  }

  private def evaluateRule(rule: Rule[Input, Output], input: Input[F])(using EvaluationContext[Input]): F[RuleResult[Input, Output]] = {
    type FBool[T] = F[Boolean]
    val evaluatedF: Input[FBool] = HKD.map2(rule.matching, input)([t] =>
      (expr, fValue) => {
        (expr: UnaryTest[t]) match {
          case UnaryTest.CatchAll => true.pure[F] // this makes evaluation lazy
          case _                  => fValue.map(expr.evaluate)
        }
      },
    )
    val result                   = for {
      evaluated <- sequence[Input, F, Const[Boolean]](evaluatedF)
      matches    = HKDUtils.collectFields(evaluated).forall(identity)
      evalResult = Option.when(matches)(rule.evaluateOutput())
    } yield RuleResult(evaluated, evalResult)
    result
  }

  // Weird implementation of the usual sequence/traverse. Collects all the fields, sequences them and reconstruct the case class
  private def sequence[CaseClass[_[_]]: HKD, G[_]: Applicative, H[_]](instance: CaseClass[[t] =>> G[H[t]]]): G[CaseClass[H]] = {
    // TODO, this case is safe only for covariant H, and G
    val collected: Vector[G[H[Any]]] = HKDUtils.collectFields[CaseClass, G[H[Any]]](instance.asInstanceOf[CaseClass[Const[G[H[Any]]]]])
    collected.sequence
      .map(values => {
        // TODO this will break for nested strcutures,
        //  but behaviour of the whole module is undefined and not supported in such case anyway
        val result: CaseClass[H] = summon[HKD[CaseClass]].construct[H]([t] => fu => values(fu.idx).asInstanceOf[H[t]])
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
          HKD.map2(input, HKD[Input].meta)([t] => (fValue, meta) => FVariable[t](meta.name, fValue, dispatcher))
        new EvaluationContext[Input] {
          override def wholeInput: Input[Expr] = variables
        }
      })
  }

  case class FVariable[T](name: String, value: F[T], dispatcher: Dispatcher[F]) extends Expr[T] {
    override def evaluate: T              = dispatcher.unsafeRunSync(value)
    override def renderExpression: String = name
  }

}
