package decisions4s.jsonlogic

import decisions4s.exprs.UnaryTest
import decisions4s.internal.{Const, Extract, Functor, HKDUtils}
import decisions4s.*
import dev.cel.common.CelVarDecl
import dev.cel.common.types.{CelKind, CelType}
import dev.cel.compiler.{CelCompiler, CelCompilerFactory}
import dev.cel.runtime.{CelRuntime, CelRuntimeFactory}

import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait ToCelType[T] {
  def tpe: CelType
}
trait FromCel[T]   {
  def read(tpe: CelType): Option[Any => T]
}

object PersistedDecisionTable {

  def load[Input[_[_]]: HKD, Output[_[_]]: HKD](
      raw: DecisionTableDTO,
      inputTypes: Input[ToCelType],
      outputReaders: Output[FromCel],
  ): Try[DecisionTable[Input, Output, HitPolicy]] = Try {
    val celCompiler = CelCompilerFactory.standardCelCompilerBuilder
      .addVarDeclarations(constructVariables(inputTypes).asJava)
      .build
    val celRuntime  = CelRuntimeFactory.standardCelRuntimeBuilder.build

    DecisionTable(
      raw.rules.map(rule => convertRule(rule, celRuntime, celCompiler, outputReaders).get),
      raw.name,
      raw.hitPolicy,
    )
  }

  private def convertRule[Input[_[_]]: {HKD as iHKD}, Output[_[_]]: {HKD as oHKD}](
      rule: DecisionTableDTO.Rule,
      runtime: CelRuntime,
      compiler: CelCompiler,
      outputReaders: Output[FromCel],
  ): Try[Rule[Input, Output]] = Try {
    val matching: EvaluationContext[Input] ?=> Input[UnaryTest]  = {
      val inputVars = convertInput[Input]
      iHKD.meta.mapK1([t] =>
        meta => {
          val predicateString = rule.inputs.apply(meta.name)
          val expr            = convertExpression(runtime, compiler, predicateString, fromCelBool, inputVars).get
          UnaryTest.Bool(expr)
        },
      )
    }
    // TODO passing input
    given Extract[FromCel] with {
      extension [T](ft: FromCel[T]) {
        def extract: T = ???
      }
    }
    given Functor[FromCel] with {
      extension [T](ft: FromCel[T]) {
        def map[T1](f: T => T1): FromCel[T1] = new FromCel[T1] {
          override def read(tpe: CelType): Option[Any => T1] = ft.read(tpe).map(_.andThen(f))
        }
      }
    }
    val output: EvaluationContext[Input] ?=> Output[OutputValue] = {
      val inputVars = convertInput[Input]
      oHKD.map2(outputReaders, oHKD.meta)([t] =>
        (reader, meta) => {
          val predicateString = rule.outputs.apply(meta.name)
          convertExpression(runtime, compiler, predicateString, reader, inputVars).get
        },
      )
    }
    Rule(matching, output, rule.annotation)
  }

  private def constructVariables[Input[_[_]]](types: Input[ToCelType])(using inputHKD: HKD[Input]): Seq[CelVarDecl] = {
    val typesSeq: Seq[CelType] = HKDUtils.collectFields(types.mapK([t] => tc => tc.tpe))
    val names                  = inputHKD.fieldNames
    names.zip(typesSeq).map(CelVarDecl.newVarDeclaration)
  }

  private def convertExpression[T](
      runtime: CelRuntime,
      compiler: CelCompiler,
      expression: String,
      fromCel: FromCel[T],
      inputVars: InputVars,
  ): Try[CelExpression[T]] = Try {
    val compiled = compiler.compile(expression)
    val ast      = compiled.getAst
    val reader   = fromCel.read(ast.getResultType).getOrElse(throw new Exception(s"Unexpected result type ${ast.getResultType}"))
    val program  = runtime.createProgram(ast)
    CelExpression(expression, program, reader, inputVars)
  }

  private def convertInput[Input[_[_]]: {HKD as iHKD}](using ec: EvaluationContext[Input]): InputVars = {
    val wholeInput: Input[Expr] = ec.wholeInput
    lazy val transformed             = HKDUtils.collectFields(wholeInput.mapK[Const[Any]]([t] => expr => expr.evaluate))
    new Expr[Map[String, Any]] {
      def evaluate: Map[String, Any] = iHKD.fieldNames.zip(transformed).toMap
      def renderExpression: String   = "you should never see this"
    }
  }

  private type InputVars = Expr[Map[String, Any]]

  private val fromCelBool: FromCel[Boolean] = (tpe: CelType) => Option.when(tpe.kind() == CelKind.BOOL)(x => x.asInstanceOf[Boolean])
}

class CelExpression[T](source: String, compiled: CelRuntime.Program, reader: Any => T, input: Expr[Map[String, Any]]) extends Expr[T] {
  def evaluate: T              = compiled.eval(input.evaluate.asJava).pipe(reader)
  def renderExpression: String = source
}
