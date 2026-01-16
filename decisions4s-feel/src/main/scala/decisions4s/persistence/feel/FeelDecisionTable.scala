package decisions4s.persistence.feel

import decisions4s.*
import decisions4s.exprs.UnaryTest
import decisions4s.internal.{Const, Extract, Functor, HKDUtils}
import decisions4s.persistence.DecisionTableDTO
import org.camunda.feel.FeelEngine
import org.camunda.feel.api.FeelEngineApi

import scala.util.Try

object FeelDecisionTable {

  val defaultEngine: FeelEngineApi = new FeelEngineApi(new FeelEngine())

  def load[Input[_[_]]: HKD, Output[_[_]]: HKD, HP <: HitPolicy](
      raw: DecisionTableDTO,
      outputReaders: Output[FromFeel],
      hitPolicy: HP,
      engine: FeelEngineApi = defaultEngine,
  ): Try[DecisionTable[Input, Output, HP]] = Try {
    DecisionTable(
      raw.rules.map(rule => convertRule(rule, outputReaders, engine).get),
      raw.name,
      hitPolicy,
    )
  }

  private def convertRule[Input[_[_]]: {HKD as iHKD}, Output[_[_]]: {HKD as oHKD}](
      rule: DecisionTableDTO.Rule,
      outputReaders: Output[FromFeel],
      engine: FeelEngineApi,
  ): Try[Rule[Input, Output]] = Try {
    val matching: EvaluationContext[Input] ?=> Input[UnaryTest] = {
      val inputVars = convertInput[Input]
      iHKD.meta.mapK1([t] =>
        meta => {
          val predicateString = rule.inputs.apply(meta.name)
          UnaryTest.Bool(FeelUnaryTestExpression(predicateString, engine, inputVars, meta.name))
        },
      )
    }

    given Extract[FromFeel] with {
      extension [T](ft: FromFeel[T]) {
        def extract: T = ???
      }
    }
    given Functor[FromFeel] with {
      extension [T](ft: FromFeel[T]) {
        def map[T1](f: T => T1): FromFeel[T1] = new FromFeel[T1] {
          override def read: Any => T1 = ft.read.andThen(f)
        }
      }
    }

    val output: EvaluationContext[Input] ?=> Output[OutputValue] = {
      val inputVars = convertInput[Input]
      oHKD.map2(outputReaders, oHKD.meta)([t] =>
        (reader, meta) => {
          val expressionString = rule.outputs.apply(meta.name)
          FeelExpression(expressionString, engine, reader.read, inputVars)
        },
      )
    }

    Rule(matching, output, rule.annotation)
  }

  private def convertInput[Input[_[_]]: {HKD as iHKD}](using ec: EvaluationContext[Input]): Expr[Map[String, Any]] = {
    val wholeInput: Input[Expr]  = ec.wholeInput
    lazy val transformed         = HKDUtils.collectFields(wholeInput.mapK[Const[Any]]([t] => expr => expr.evaluate))
    new Expr[Map[String, Any]] {
      def evaluate: Map[String, Any] = iHKD.fieldNames.zip(transformed).toMap
      def renderExpression: String   = "input context"
    }
  }

}
