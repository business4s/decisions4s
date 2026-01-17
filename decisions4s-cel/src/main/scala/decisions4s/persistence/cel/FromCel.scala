package decisions4s.persistence.cel

import dev.cel.common.types.{CelType, SimpleType}

trait FromCel[T] {
  def read(tpe: CelType): Option[Any => T]
}

object FromCel {

  given FromCel[Int] with {
    // CEL INT is 64-bit, returned as Long
    def read(tpe: CelType): Option[Any => Int] =
      Option.when(tpe == SimpleType.INT)(x => Math.toIntExact(x.asInstanceOf[Long]))
  }

  given FromCel[Long] with {
    def read(tpe: CelType): Option[Any => Long] =
      Option.when(tpe == SimpleType.INT)(x => x.asInstanceOf[Long])
  }

  given FromCel[Double] with {
    def read(tpe: CelType): Option[Any => Double] =
      Option.when(tpe == SimpleType.DOUBLE)(x => x.asInstanceOf[Double])
  }

  given FromCel[Float] with {
    // CEL uses Double for floating point
    def read(tpe: CelType): Option[Any => Float] =
      Option.when(tpe == SimpleType.DOUBLE)(x => x.asInstanceOf[Double].toFloat)
  }

  given FromCel[String] with {
    def read(tpe: CelType): Option[Any => String] =
      Option.when(tpe == SimpleType.STRING)(x => x.asInstanceOf[String])
  }

  given FromCel[Boolean] with {
    def read(tpe: CelType): Option[Any => Boolean] =
      Option.when(tpe == SimpleType.BOOL)(x => x.asInstanceOf[Boolean])
  }

}
