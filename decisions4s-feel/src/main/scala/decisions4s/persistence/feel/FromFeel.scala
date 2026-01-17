package decisions4s.persistence.feel

/** Converts FEEL evaluation results to Scala types.
  *
  * FEEL uses BigDecimal for all numbers, which is different from CEL. This trait handles the conversion from FEEL's internal representation to the
  * expected Scala types.
  */
trait FromFeel[T] {
  def read: Any => T
}

object FromFeel {

  given FromFeel[Int] with {
    // FEEL numbers are BigDecimal
    def read: Any => Int = {
      case n: java.math.BigDecimal => n.intValue()
      case n: BigDecimal           => n.toInt
      case n: Number               => n.intValue()
    }
  }

  given FromFeel[Long] with {
    def read: Any => Long = {
      case n: java.math.BigDecimal => n.longValue()
      case n: BigDecimal           => n.toLong
      case n: Number               => n.longValue()
    }
  }

  given FromFeel[Double] with {
    def read: Any => Double = {
      case n: java.math.BigDecimal => n.doubleValue()
      case n: BigDecimal           => n.toDouble
      case n: Number               => n.doubleValue()
    }
  }

  given FromFeel[Float] with {
    def read: Any => Float = {
      case n: java.math.BigDecimal => n.floatValue()
      case n: BigDecimal           => n.toFloat
      case n: Number               => n.floatValue()
    }
  }

  given FromFeel[String] with {
    def read: Any => String = _.asInstanceOf[String]
  }

  given FromFeel[Boolean] with {
    def read: Any => Boolean = _.asInstanceOf[Boolean]
  }

}
