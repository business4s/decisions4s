package decisions4s.testing

import org.scalatest.Assertions.Equalizer

trait HiddenTripleEquals {

  // hides === from scalates by ambiguing the conversion
  implicit def convertToEqualizer2[T](left: T): Equalizer[T] = ???
  implicit def convertToEqualizer3[T](left: T): Equalizer[T] = ???
}
