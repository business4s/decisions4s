package decisions4s

import decisions4s.Tuple2K
import shapeless3.deriving.{K11, ~>}

/** Specialised typeclass to expose operations on higher kinded data (case-classes where each field is wrapped in F[_])
  */
trait HKD[F[_[_]]] {

  // allows to instantiate the case class
  def pure[A[_]](f: [t] => () => A[t]): F[A]

  extension [A[_]](af: F[A]) {
    // allows to map over case class fields
    def mapK[B[_]](f: A ~> B): F[B]

    // allows to merge case classes together
    def productK[B[_]](fb: F[B]): F[Tuple2K[A, B]]
  }

}

object HKD {

  def apply[F[_[_]]](using hkd: HKD[F]): HKD[F] = hkd

  given [T]: HKD[K11.Id[T]] with {
    override def pure[A[_]](f: [t] => () => A[t]): A[T] = f[T]()

    extension [A[_]](at: A[T]){
      def mapK[B[_]](f: A ~> B): B[T]                = f(at)
      def productK[B[_]](fb: B[T]): Tuple2K[A, B][T] = (at, fb)
    }
  }

  given shapeGen[F[_[_]]](using inst: => K11.ProductInstances[HKD, F]): HKD[F] with {
    override def pure[A[_]](f: [t] => () => A[t]): F[A] = inst.construct([t[_[_]]] => (p: HKD[t]) => p.pure([t1] => () => f[t1]()))

    extension [A[_]](fa: F[A]) {
      def mapK[B[_]](f: A ~> B): F[B] =
        inst.map(fa)([t[_[_]]] => (ft: HKD[t], ta: t[A]) => ft.mapK(ta)(f))

      def productK[B[_]](fb: F[B]): F[Tuple2K[A, B]] =
        inst.map2(fa, fb)([t[_[_]]] => (s: HKD[t], ta: t[A], tb: t[B]) => s.productK(ta)(tb))
    }

  }

  inline def derived[F[_[_]]](using gen: K11.ProductGeneric[F]): HKD[F] = shapeGen

}
