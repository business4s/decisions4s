package decisions4s.persistence.feel

import org.camunda.feel.FeelEngine
import org.camunda.feel.api.{FeelEngineApi, SuccessfulEvaluationResult}
import org.scalatest.freespec.AnyFreeSpec

class FeelTypeConversionTest extends AnyFreeSpec {

  private val engine = new FeelEngineApi(new FeelEngine())

  "FromFeel instances" - {

    "Int" - {
      "should pass through FEEL correctly" in {
        val result = evalExpression[Int]("x", Map("x" -> 42))
        assert(result == 42)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Int]("x + 10", Map("x" -> 32))
        assert(result == 42)
      }

      "should handle negative values" in {
        val result = evalExpression[Int]("-x", Map("x" -> 42))
        assert(result == -42)
      }
    }

    "Long" - {
      "should pass through FEEL correctly" in {
        val result = evalExpression[Long]("x", Map("x" -> 42L))
        assert(result == 42L)
      }

      "should handle large values" in {
        val largeValue = Int.MaxValue.toLong + 1
        val result     = evalExpression[Long]("x", Map("x" -> largeValue))
        assert(result == largeValue)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Long]("x * 2", Map("x" -> 21L))
        assert(result == 42L)
      }
    }

    "Double" - {
      "should pass through FEEL correctly" in {
        val result = evalExpression[Double]("x", Map("x" -> 3.14))
        assert(Math.abs(result - 3.14) < 0.001)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Double]("x * 2", Map("x" -> 1.5))
        assert(result == 3.0)
      }

      "should handle division" in {
        val result = evalExpression[Double]("x / 2", Map("x" -> 10.0))
        assert(result == 5.0)
      }
    }

    "Float" - {
      "should pass through FEEL correctly (via Double)" in {
        val result = evalExpression[Float]("x", Map("x" -> 3.14))
        assert(Math.abs(result - 3.14f) < 0.001f)
      }

      "should work in arithmetic expressions" in {
        val result = evalExpression[Float]("x * 2", Map("x" -> 1.5))
        assert(result == 3.0f)
      }
    }

    "String" - {
      "should pass through FEEL correctly" in {
        val result = evalExpression[String]("x", Map("x" -> "hello"))
        assert(result == "hello")
      }

      "should work with string concatenation" in {
        val result = evalExpression[String]("""x + " world"""", Map("x" -> "hello"))
        assert(result == "hello world")
      }

      "should handle empty strings" in {
        val result = evalExpression[String]("x", Map("x" -> ""))
        assert(result == "")
      }

      "should handle unicode" in {
        val result = evalExpression[String]("x", Map("x" -> "hello 世界"))
        assert(result == "hello 世界")
      }
    }

    "Boolean" - {
      "should pass through FEEL correctly" in {
        assert(evalExpression[Boolean]("x", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("x", Map("x" -> false)) == false)
      }

      "should work with logical operations" in {
        assert(evalExpression[Boolean]("x and true", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("x and true", Map("x" -> false)) == false)
        assert(evalExpression[Boolean]("x or false", Map("x" -> true)) == true)
        assert(evalExpression[Boolean]("not(x)", Map("x" -> true)) == false)
      }
    }
  }

  private def evalExpression[T: FromFeel](
      expression: String,
      context: Map[String, Any],
  ): T = {
    val fromFeel = summon[FromFeel[T]]
    engine.evaluateExpression(expression, context) match {
      case SuccessfulEvaluationResult(result, _) => fromFeel.read(result)
      case other                                 => throw new RuntimeException(s"Evaluation failed: $other")
    }
  }
}
