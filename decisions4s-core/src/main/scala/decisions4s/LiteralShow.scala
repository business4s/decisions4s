package decisions4s

import java.time.{Duration, Period}

/** Typeclass controlling how the given type should render when using as a literal in rules definition
  */
trait LiteralShow[-T] {
  def show(v: T): String
}

object LiteralShow {

  given LiteralShow[Duration] = _.toString
  given LiteralShow[Period]   = _.toString
  given LiteralShow[Boolean]  = _.toString
  given LiteralShow[Int]      = _.toString
  given LiteralShow[Long]     = _.toString
  given LiteralShow[Double]   = _.toString

}
