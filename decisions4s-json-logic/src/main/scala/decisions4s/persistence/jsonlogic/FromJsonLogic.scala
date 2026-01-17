package decisions4s.persistence.jsonlogic

trait FromJsonLogic[T] {
  def read: Any => T
}

object FromJsonLogic {

  given FromJsonLogic[Int] with {
    def read: Any => Int = {
      case n: java.lang.Double  => n.intValue()
      case n: java.lang.Integer => n.intValue()
      case n: java.lang.Long    => n.intValue()
      case n: Number            => n.intValue()
    }
  }

  given FromJsonLogic[Long] with {
    def read: Any => Long = {
      case n: java.lang.Double  => n.longValue()
      case n: java.lang.Integer => n.longValue()
      case n: java.lang.Long    => n.longValue()
      case n: Number            => n.longValue()
    }
  }

  given FromJsonLogic[Double] with {
    def read: Any => Double = {
      case n: java.lang.Double  => n.doubleValue()
      case n: java.lang.Integer => n.doubleValue()
      case n: java.lang.Long    => n.doubleValue()
      case n: Number            => n.doubleValue()
    }
  }

  given FromJsonLogic[Float] with {
    def read: Any => Float = {
      case n: java.lang.Double  => n.floatValue()
      case n: java.lang.Integer => n.floatValue()
      case n: java.lang.Long    => n.floatValue()
      case n: Number            => n.floatValue()
    }
  }

  given FromJsonLogic[String] with {
    def read: Any => String = _.asInstanceOf[String]
  }

  given FromJsonLogic[Boolean] with {
    def read: Any => Boolean = _.asInstanceOf[Boolean]
  }

}
