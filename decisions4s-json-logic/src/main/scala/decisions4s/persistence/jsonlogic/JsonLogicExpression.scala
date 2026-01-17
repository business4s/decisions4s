package decisions4s.persistence.jsonlogic

import decisions4s.Expr
import io.github.jamsesso.jsonlogic.JsonLogic

import scala.jdk.CollectionConverters.*

class JsonLogicExpression[T](
    source: String,
    engine: JsonLogic,
    reader: Any => T,
    input: Expr[Map[String, Any]],
) extends Expr[T] {

  def evaluate: T = {
    val context = input.evaluate.asJava
    val result  = engine.apply(source, context)
    reader(result)
  }

  def renderExpression: String = source
}
