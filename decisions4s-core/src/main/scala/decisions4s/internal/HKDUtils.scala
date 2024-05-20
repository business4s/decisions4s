package decisions4s.internal

import decisions4s.HKD
import decisions4s.HKD.syntax.*
import shapeless3.deriving.~>

import scala.collection.mutable.ListBuffer

object HKDUtils {

  type Const[T] = [t] =>> T

  def collectFields[F[_[_]]: HKD, T](instance: F[Const[T]]): Seq[T] = {
    val result = ListBuffer[T]()
    type Void[T] = Any
    val gatherName: Const[T] ~> Void = [t] => (fa: T) => result.append(fa)
    val _                     = instance.mapK(gatherName)
    result.toList
  }

}
