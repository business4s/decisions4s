package decisions4s.internal

import decisions4s.Tuple2K

trait Functor[F[_]] {
  extension [T](ft: F[T]) {
    def map[T1](f: T => T1): F[T1]
  }
}

object Functor {

  given const[A]: Functor[[t] =>> A] with {
    extension [T](ft: A) {
      def map[T1](f: T => T1): A = ft
    }
  }
  given id: Functor[[t] =>> t] with       {
    extension [T](ft: T) {
      def map[T1](f: T => T1): T1 = f(ft)
    }
  }

  given option: Functor[Option] = new Functor[Option] {
    extension [T](ft: Option[T]) {
      def map[T1](f: T => T1): Option[T1] = ft.map(f)
    }
  }

  given compound[F[_], G[_]](using ff: Functor[F], fg: Functor[G]): Functor[[t] =>> F[G[t]]] with {
    extension [T](ft: F[G[T]]) {
      def map[T1](f: T => T1): F[G[T1]] = ff.map(ft)(x => fg.map(x)(f))
    }
  }

  given tuple2K[A[_], B[_]](using Functor[A], Functor[B]): Functor[Tuple2K[A, B]] = new Functor[Tuple2K[A, B]] {
    extension [T](ft: (A[T], B[T])) {
      def map[T1](f: T => T1): (A[T1], B[T1]) = (ft._1.map(f), ft._2.map(f))
    }
  }

}
