package decisions4s.persistence.jsonlogic

import io.github.jamsesso.jsonlogic.JsonLogic
import org.scalatest.freespec.AnyFreeSpec

import scala.jdk.CollectionConverters.*

class JsonLogicTypeConversionTest extends AnyFreeSpec {

  private val engine = new JsonLogic()

  "FromJsonLogic instances" - {

    "Int" - {
      "should pass through json-logic correctly" in {
        val result = evalExpression[Int]("""{"var":"x"}""", Map("x" -> 42))
        assert(result == 42)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Int]("""{"+":[{"var":"x"}, 10]}""", Map("x" -> 32))
        assert(result == 42)
      }

      "should handle negative values" in {
        val result = evalExpression[Int]("""{"*":[{"var":"x"}, -1]}""", Map("x" -> 42))
        assert(result == -42)
      }
    }

    "Long" - {
      "should pass through json-logic correctly" in {
        val result = evalExpression[Long]("""{"var":"x"}""", Map("x" -> 42L))
        assert(result == 42L)
      }

      "should handle large values" in {
        val largeValue = Int.MaxValue.toLong + 1
        val result     = evalExpression[Long]("""{"var":"x"}""", Map("x" -> largeValue.toDouble))
        assert(result == largeValue)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Long]("""{"*":[{"var":"x"}, 2]}""", Map("x" -> 21L))
        assert(result == 42L)
      }
    }

    "Double" - {
      "should pass through json-logic correctly" in {
        val result = evalExpression[Double]("""{"var":"x"}""", Map("x" -> 3.14))
        assert(Math.abs(result - 3.14) < 0.001)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Double]("""{"*":[{"var":"x"}, 2]}""", Map("x" -> 1.5))
        assert(result == 3.0)
      }

      "should handle division" in {
        val result = evalExpression[Double]("""{"/":[{"var":"x"}, 2]}""", Map("x" -> 10.0))
        assert(result == 5.0)
      }
    }

    "Float" - {
      "should pass through json-logic correctly (via Double)" in {
        val result = evalExpression[Float]("""{"var":"x"}""", Map("x" -> 3.14))
        assert(Math.abs(result - 3.14f) < 0.001f)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Float]("""{"*":[{"var":"x"}, 2]}""", Map("x" -> 1.5))
        assert(result == 3.0f)
      }
    }

    "String" - {
      "should pass through json-logic correctly" in {
        val result = evalExpression[String]("""{"var":"x"}""", Map("x" -> "hello"))
        assert(result == "hello")
      }

      "should work with string concatenation" in {
        val result = evalExpression[String]("""{"cat":[{"var":"x"}, " world"]}""", Map("x" -> "hello"))
        assert(result == "hello world")
      }

      "should handle empty strings" in {
        val result = evalExpression[String]("""{"var":"x"}""", Map("x" -> ""))
        assert(result == "")
      }

      "should handle unicode" in {
        val result = evalExpression[String]("""{"var":"x"}""", Map("x" -> "hello 世界"))
        assert(result == "hello 世界")
      }
    }

    "Boolean" - {
      "should pass through json-logic correctly" in {
        assert(evalExpression[Boolean]("""{"var":"x"}""", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("""{"var":"x"}""", Map("x" -> false)) == false)
      }

      "should work with logical operations" in {
        assert(evalExpression[Boolean]("""{"and":[{"var":"x"}, true]}""", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("""{"and":[{"var":"x"}, true]}""", Map("x" -> false)) == false)
        assert(evalExpression[Boolean]("""{"or":[{"var":"x"}, false]}""", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("""{"!":[{"var":"x"}]}""", Map("x" -> true)) == false)
      }
    }
  }

  private def evalExpression[T: FromJsonLogic](
      expression: String,
      context: Map[String, Any],
  ): T = {
    val fromJsonLogic = summon[FromJsonLogic[T]]
    val result        = engine.apply(expression, context.asJava)
    fromJsonLogic.read(result)
  }
}
