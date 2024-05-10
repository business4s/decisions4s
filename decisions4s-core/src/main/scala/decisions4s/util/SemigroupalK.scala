package decisions4s.util

import cats.data.Tuple2K
import shapeless3.deriving.K11

trait SemigroupalK[H[_[_]]] {
  def productK[F[_], G[_]](af: H[F], ag: H[G]): H[[t] =>> Tuple2K[F, G, t]]
}

object SemigroupalK {
  inline def apply[H[_[_]]](using fh: SemigroupalK[H]): SemigroupalK[H] = fh

  given [T]: SemigroupalK[K11.Id[T]] with {
    override def productK[F[_], G[_]](af: F[T], ag: G[T]): Tuple2K[F, G, T] = Tuple2K(af, ag)
  }

  given semigroupalKGen[H[_[_]]](using inst: => K11.ProductInstances[SemigroupalK, H]): SemigroupalK[H] with {
    override def productK[F[_], G[_]](af: H[F], ag: H[G]): H[[t] =>> Tuple2K[F, G, t]] =
      inst.map2(af, ag)([t[_[_]]] => (s: SemigroupalK[t], ta: t[F], tb: t[G]) => s.productK(ta, tb))
  }

  inline def derived[F[_[_]]](using gen: K11.ProductGeneric[F]): SemigroupalK[F] = semigroupalKGen

  object syntax {
    extension [H[_[_]], F[_]](hf: H[F])(using sg: SemigroupalK[H]) {
      def productK[G[_]](hg: H[G]): H[[t] =>> Tuple2K[F, G, t]] = sg.productK(hf, hg)
    }
  }
}
