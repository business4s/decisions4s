package decisions4s.persistence.cel

import dev.cel.common.CelVarDecl
import dev.cel.compiler.CelCompilerFactory
import dev.cel.runtime.CelRuntimeFactory
import org.scalatest.freespec.AnyFreeSpec

import scala.jdk.CollectionConverters.*

class CelTypeConversionTest extends AnyFreeSpec {

  "ToCelType and FromCel instances" - {

    "Int" - {
      "should pass through CEL correctly" in {
        val result = compileAndEval[Int]("x", "x", 42) // CEL expects Long for INT
        assert(result == 42)
      }

      "should work in arithmetic expressions" in {
        val result = compileAndEval[Int]("x", "x + 10", 32)
        assert(result == 42)
      }

      "should work in ternary expressions returning Int" in {
        // abs(-42) using ternary: x > 0 ? x : -x
        val result = compileAndEval[Int]("x", "x > 0 ? x : -x", -42)
        assert(result == 42) // -(-42) = 42
      }
    }

    "Long" - {
      "should pass through CEL correctly" in {
        val result = compileAndEval[Long]("x", "x", 42L)
        assert(result == 42L)
      }

      "should handle large values" in {
        val largeValue = Long.MaxValue - 1
        val result     = compileAndEval[Long]("x", "x", largeValue)
        assert(result == largeValue)
      }

      "should work in arithmetic expressions" in {
        val result = compileAndEval[Long]("x", "x * 2", 21L)
        assert(result == 42L)
      }
    }

    "Double" - {
      "should pass through CEL correctly" in {
        val result = compileAndEval[Double]("x", "x", 3.14)
        assert(result == 3.14)
      }

      "should work in arithmetic expressions" in {
        val result = compileAndEval[Double]("x", "x * 2.0", 1.5)
        assert(result == 3.0)
      }

      "should handle special values" in {
        assert(compileAndEval[Double]("x", "x", Double.PositiveInfinity) == Double.PositiveInfinity)
        assert(compileAndEval[Double]("x", "x", Double.NegativeInfinity) == Double.NegativeInfinity)
      }
    }

    "Float" - {
      "should pass through CEL correctly (via Double)" in {
        val result = compileAndEval[Float]("x", "x", 3.14)
        assert(Math.abs(result - 3.14f) < 0.001f)
      }

      "should work in arithmetic expressions" in {
        val result = compileAndEval[Float]("x", "x * 2.0", 1.5)
        assert(result == 3.0f)
      }
    }

    "String" - {
      "should pass through CEL correctly" in {
        val result = compileAndEval[String]("x", "x", "hello")
        assert(result == "hello")
      }

      "should work with string operations" in {
        val result = compileAndEval[String]("x", """x + " world"""", "hello")
        assert(result == "hello world")
      }

      "should handle empty strings" in {
        val result = compileAndEval[String]("x", "x", "")
        assert(result == "")
      }

      "should handle unicode" in {
        val result = compileAndEval[String]("x", "x", "hello \u4e16\u754c")
        assert(result == "hello \u4e16\u754c")
      }
    }

    "Boolean" - {
      "should pass through CEL correctly" in {
        assert(compileAndEval[Boolean]("x", "x", true) == true)
        assert(compileAndEval[Boolean]("x", "x", false) == false)
      }

      "should work with logical operations" in {
        assert(compileAndEval[Boolean]("x", "x && true", true) == true)
        assert(compileAndEval[Boolean]("x", "x && true", false) == false)
        assert(compileAndEval[Boolean]("x", "x || false", true) == true)
        assert(compileAndEval[Boolean]("x", "!x", true) == false)
      }
    }
  }

  private def compileAndEval[T: {ToCelType, FromCel}](
      varName: String,
      expression: String,
      inputValue: T,
  ): T = {
    val celType  = summon[ToCelType[T]].tpe
    val fromCel  = summon[FromCel[T]]
    val compiler = CelCompilerFactory.standardCelCompilerBuilder
      .addVarDeclarations(Seq(CelVarDecl.newVarDeclaration(varName, celType)).asJava)
      .build
    val runtime  = CelRuntimeFactory.standardCelRuntimeBuilder.build
    val ast      = compiler.compile(expression).getAst
    val program  = runtime.createProgram(ast)
    val result   = program.eval(Map(varName -> inputValue).asJava)
    fromCel.read(ast.getResultType).get(result)
  }
}
