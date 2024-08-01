package decisions4s.exprs

import decisions4s.LiteralShow
import munit.FunSuite

import java.time.*

class LiteralTest extends FunSuite {

  import TestUtils.*

  // https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-data-types/
  // TODO property based
  test("built-ins") {
    checkLiteral(1)
    checkLiteral(2.0)
    checkLiteral(3L)
    checkLiteral("xxx")
    checkLiteral(true)
    checkLiteral(false)

    checkLiteral(LocalDate.now)
    // value -> feel -> value trims nanos()
    checkLiteralAdj(LocalTime.now)(_.withNano(0))
    checkLiteralAdj(OffsetTime.now)(_.withNano(0))
    checkLiteralAdj(LocalDateTime.now)(_.withNano(0))
    checkLiteralAdj(OffsetDateTime.now)(_.toZonedDateTime.withNano(0))        // feel parses offset as zoned
    checkLiteralAdj(ZonedDateTime.now)(_.withNano(0))
    checkLiteral(Duration.ofHours(11))
    checkLiteralAdj(Period.ofDays(12))(x => Duration.ofHours(x.getDays * 24)) // period below 1 month is parsed as duration
    checkLiteral(Period.ofMonths(2))

    checkLiteral(List(1, 2, 3))
    checkLiteral(Vector(1, 2, 3))

    // context is currently unsupported
    // https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-data-types/#context

  }

  private def checkLiteral[T: LiteralShow](v: T): Unit                                  = checkLiteralAdj(v)(identity)
  private def checkLiteralAdj[T: LiteralShow](v: T)(expectedAdjustment: T => Any): Unit = checkExpression(Literal(v), v, Some(expectedAdjustment(v)))

}
