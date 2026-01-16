package decisions4s.persistence.cel

import dev.cel.common.types.{CelType, SimpleType}

trait ToCelType[T] {
  def tpe: CelType
}

object ToCelType {

  given ToCelType[Int] with {
    override def tpe: CelType = SimpleType.INT
  }

  given ToCelType[Long] with {
    override def tpe: CelType = SimpleType.INT
  }

  given ToCelType[Double] with {
    override def tpe: CelType = SimpleType.DOUBLE
  }

  given ToCelType[Float] with {
    // CEL uses Double for floating point; Float will be widened
    override def tpe: CelType = SimpleType.DOUBLE
  }

  given ToCelType[String] with {
    override def tpe: CelType = SimpleType.STRING
  }

  given ToCelType[Boolean] with {
    override def tpe: CelType = SimpleType.BOOL
  }

}
