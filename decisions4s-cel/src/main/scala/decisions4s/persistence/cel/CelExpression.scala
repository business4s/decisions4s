package decisions4s.persistence.cel

import decisions4s.Expr
import dev.cel.runtime.CelRuntime

import scala.jdk.CollectionConverters.given
import scala.util.chaining.scalaUtilChainingOps 

class CelExpression[T](source: String, compiled: CelRuntime.Program, reader: Any => T, input: Expr[Map[String, Any]]) extends Expr[T] {
  def evaluate: T              = compiled.eval(input.evaluate.asJava).pipe(reader)
  def renderExpression: String = source
}
