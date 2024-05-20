package decisions4s

import decisions4s.Tuple2K
import shapeless3.deriving.{K11, ~>}

/**
 * Specialised typeclass to expose operations on higher kinded data (case-classes where each field is wrapped in F[_])
 */
trait HKD[F[_[_]]] {

  // allows to map over case class fields
  def mapK[A[_], B[_]](af: F[A])(f: A ~> B): F[B]

  // allows to instantiate the case class
  def pure[A[_]](f: [t] => () => A[t]): F[A]

  // allows to merge case classes together
  def productK[A[_], B[_]](af: F[A], ag: F[B]): F[Tuple2K[A, B]]

}

object HKD {

  def apply[F[_[_]]](using hkd: HKD[F]): HKD[F] = hkd

  given [T]: HKD[K11.Id[T]] with {
    override def mapK[A[_], B[_]](at: A[T])(f: A ~> B): B[T]                = f(at)
    override def pure[A[_]](f: [t] => () => A[t]): A[T]                     = f[T]()
    override def productK[F[_], G[_]](af: F[T], ag: G[T]): Tuple2K[F, G][T] = (af, ag)
  }

  given shapeGen[F[_[_]]](using inst: => K11.ProductInstances[HKD, F]): HKD[F] with {
    def mapK[A[_], B[_]](ha: F[A])(f: A ~> B): F[B] =
      inst.map(ha)([t[_[_]]] => (ft: HKD[t], ta: t[A]) => ft.mapK(ta)(f))

    override def pure[A[_]](f: [t] => () => A[t]): F[A] = inst.construct([t[_[_]]] => (p: HKD[t]) => p.pure([t1] => () => f[t1]()))

    override def productK[A[_], B[_]](af: F[A], ag: F[B]): F[Tuple2K[A, B]] =
      inst.map2(af, ag)([t[_[_]]] => (s: HKD[t], ta: t[A], tb: t[B]) => s.productK(ta, tb))
  }

  inline def derived[F[_[_]]](using gen: K11.ProductGeneric[F]): HKD[F] = shapeGen

  object syntax {
    extension [H[_[_]], A[_]](hf: H[A])(using fk: HKD[H]) {
      def mapK[G[_]](f: A ~> G): H[G] = fk.mapK(hf)(f)
      def productK[B[_]](hg: H[B]): H[Tuple2K[A, B]] = fk.productK(hf, hg)
    }
  }

}
