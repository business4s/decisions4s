package decisions4s.util

import shapeless3.deriving.K11

trait PureK[H[_[_]]] {
  def pure[A[_]](f: [t] => () => A[t]): H[A]
}

object PureK {
  inline def apply[H[_[_]]](using fh: PureK[H]): PureK[H] = fh

  given [T]: PureK[K11.Id[T]] with {
    override def pure[A[_]](f: [t] => () => A[t]): A[T] = f[T]()
  }

  given pureKGen [H[_[_]]](using inst: => K11.ProductInstances[PureK, H]): PureK[H] with {

    override def pure[A[_]](f: [t] => () => A[t]): H[A] = inst.construct([t[_[_]]] => (p: PureK[t]) => p.pure([t1] => () => f[t1]()))
  }

  inline def derived[F[_[_]]](using gen: K11.ProductGeneric[F]): PureK[F] = pureKGen

}
