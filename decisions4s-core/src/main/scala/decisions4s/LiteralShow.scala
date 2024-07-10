package decisions4s

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, OffsetTime, Period, ZonedDateTime}

/** Typeclass controlling how the given type should render when using as a literal in rules definition
  */
trait LiteralShow[-T] {
  def show(v: T): String
}

object LiteralShow {

  given LiteralShow[Boolean]        = _.toString
  given LiteralShow[Int]            = _.toString
  given LiteralShow[Long]           = _.toString
  given LiteralShow[Double]         = _.toString
  given LiteralShow[BigDecimal]     = _.toString()
  given LiteralShow[String]         = x => s"\"$x\""
  given LiteralShow[LocalDate]      = temporal("yyyy-MM-dd")
  given LiteralShow[LocalTime]      = temporal("HH:mm:ss")
  given LiteralShow[OffsetTime]     = temporal("HH:mm:ssXXX")
  given LiteralShow[LocalDateTime]  = temporal("yyyy-MM-dd'T'HH:mm:ss")
  given LiteralShow[OffsetDateTime] = temporal("yyyy-MM-dd'T'HH:mm:ssXXX")
  given LiteralShow[ZonedDateTime]  = temporal("yyyy-MM-dd'T'HH:mm:ss@VV")
  given LiteralShow[Duration]       = x => s"@\"${x.toString}\""
  given LiteralShow[Period]         = x => s"@\"${x.toString}\""

  given [T](using ls: LiteralShow[T]): LiteralShow[Iterable[T]] = _.map(ls.show).mkString("[", ", ", "]")

  private def temporal[T <: TemporalAccessor](format: String): LiteralShow[T] = {
    val formatter = DateTimeFormatter.ofPattern(format)
    (v: T) => {
      s"@\"${formatter.format(v)}\""
    }
  }
}
