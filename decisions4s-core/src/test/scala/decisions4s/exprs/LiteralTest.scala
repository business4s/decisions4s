package decisions4s.exprs

import decisions4s.{LiteralShow, asLiteral}
import org.scalatest.freespec.AnyFreeSpec
import java.time.*

class LiteralTest extends AnyFreeSpec {

  import TestUtils.*

  // https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-data-types/
  // TODO property based
  "built-ins" in {
    checkLiteral(1)
    checkLiteral(2.0)
    checkLiteral(3L)
    checkLiteral("xxx")
    checkLiteral(true)
    checkLiteral(false)

    checkLiteral(LocalDate.now)
    // value -> feel -> value trims nanos()
    checkLiteralAdjusted(LocalTime.now)(_.withNano(0))
    checkLiteralAdjusted(OffsetTime.now)(_.withNano(0))
    checkLiteralAdjusted(LocalDateTime.now)(_.withNano(0))
    checkLiteralAdjusted(OffsetDateTime.now)(_.toZonedDateTime.withNano(0))        // feel parses offset as zoned
    checkLiteralAdjusted(ZonedDateTime.now)(_.withNano(0))
    checkLiteral(Duration.ofHours(11))
    checkLiteralAdjusted(Period.ofDays(12))(x => Duration.ofHours(x.getDays * 24)) // period below 1 month is parsed as duration
    checkLiteral(Period.ofMonths(2))

    checkLiteral(List(1, 2, 3))
    checkLiteral(Vector(1, 2, 3))

    // context is currently unsupported
    // https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-data-types/#context

  }

  "contexts" in {
    case class Foo(bar: String, baz: Int) derives LiteralShow
    val foo = Foo("aa", 3)
    checkLiteralAdjusted(foo)(x => Map("bar" -> x.bar, "baz" -> x.baz))
  }
  "options" in {
    checkLiteralAdjusted(Some(1))(_ => 1)
    checkLiteralAdjusted(None: Option[String])(_ => null)
  }
  "asLiteral" in {
    assert(1.asLiteral == Literal(1))
  }

  private def checkLiteral[T: LiteralShow](v: T): Unit                                       = checkLiteralAdjusted(v)(identity)
  private def checkLiteralAdjusted[T: LiteralShow](v: T)(expectedAdjustment: T => Any): Unit =
    checkExpression(Literal(v), v, Some(expectedAdjustment(v)))

}
