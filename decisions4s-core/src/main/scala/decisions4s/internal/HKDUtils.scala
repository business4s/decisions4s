package decisions4s.internal

import cats.arrow.FunctionK
import cats.tagless.FunctorK
import cats.tagless.FunctorK.ops.toAllFunctorKOps
import cats.~>

import scala.collection.mutable.ListBuffer

object HKDUtils {

  type Const[T] = [t] =>> T

  def collectFields[F[_[_]]: FunctorK, T](instance: F[Const[T]]): Seq[T] = {
    val result = ListBuffer[T]()
    type Void[T] = Any
    val gatherName: Const[T] ~> Void = new FunctionK[Const[T], Void] {
      override def apply[A](fa: T): Void[A] = result.append(fa)
    }
    val _                     = instance.mapK(gatherName)
    result.toList
  }

}
