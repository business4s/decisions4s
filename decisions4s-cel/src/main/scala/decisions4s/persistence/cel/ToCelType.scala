package decisions4s.persistence.cel

import dev.cel.common.types.CelType
import dev.cel.common.types.SimpleType

trait ToCelType[T] {
  def tpe: CelType
}

object ToCelType {

  given ToCelType[Int] with {
    override def tpe: CelType = SimpleType.INT
  }

  given ToCelType[String] with {
    override def tpe: CelType = SimpleType.STRING
  }

}
