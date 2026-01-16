package decisions4s.persistence.cel

import dev.cel.common.types.CelType

trait FromCel[T]   {
  def read(tpe: CelType): Option[Any => T]
}
