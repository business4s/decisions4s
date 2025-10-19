package decisions4s.internal

import decisions4s.HKD
import shapeless3.deriving.~>

object HKDUtils {

  def collectFields[F[_[_]]: HKD, T](instance: F[Const[T]]): Vector[T] = {
    val acc = Vector.newBuilder[T]
    type Void[_] = Any
    val gatherName: Const[T] ~> Void = [t] => (fa: T) => acc += fa
    val _                            = instance.mapK(gatherName)
    acc.result()
  }

}
