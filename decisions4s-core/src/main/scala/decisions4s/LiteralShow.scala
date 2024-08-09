package decisions4s

import shapeless3.deriving.{K0, Labelling}

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, OffsetTime, Period, ZonedDateTime}

/** Typeclass controlling how the given type should render when using as a literal in rules definition
  */
trait LiteralShow[-T] {
  def show(v: T): String
}

object LiteralShow {

  def apply[T](using ls: LiteralShow[T]): LiteralShow[T] = ls

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
  given [T](using ls: LiteralShow[T]): LiteralShow[Option[T]]   = _.map(ls.show).getOrElse("null")

  given [T]: LiteralShow[Expr[T]]        = _.renderExpression
  given [T]: LiteralShow[OutputValue[T]] = _.renderExpression

  // should we replace with HKD.fieldNames?
  given showGen[T](using inst: K0.ProductInstances[LiteralShow, T], labelling: Labelling[T]): LiteralShow[T] with {
    def show(t: T): String =
      if labelling.elemLabels.isEmpty then labelling.label
      else
        labelling.elemLabels.zipWithIndex
          .map((label, i) => s"$label: ${inst.project(t)(i)([t] => (st: LiteralShow[t], pt: t) => st.show(pt))}".indent(2).stripSuffix("\n"))
          .mkString(s"{\n", ",\n", "\n}")
  }

  inline def derived[A](using gen: K0.ProductGeneric[A]): LiteralShow[A] = showGen

  private def temporal[T <: TemporalAccessor](format: String): LiteralShow[T] = {
    val formatter = DateTimeFormatter.ofPattern(format)
    (v: T) => {
      s"@\"${formatter.format(v)}\""
    }
  }

  extension [T](x: T)(using ls: LiteralShow[T]) {
    def showAsLiteral: String = ls.show(x)
  }
}
