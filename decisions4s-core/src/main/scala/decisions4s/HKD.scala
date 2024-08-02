package decisions4s

import decisions4s.internal.HKDUtils
import shapeless3.deriving.K11.Id
import shapeless3.deriving.{Const, K11, Labelling, ~>}

trait Extract[F[_]]{
  extension [T](ft: F[T]){
    def extract: T
  }
}

object Extract {
  given id: Extract[[T] =>> T] with {
    extension [T](ft: T) {
      def extract: T = ft
    }
  }

}

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

case class Meta[T](index: Int, name: String, value: Option[T])

object Meta {
  given Extract[Meta] with {
    extension [T](ft: Meta[T]) {
      def extract = ft.value.get
    }
  }
  given Functor[Meta] with {
    extension [T](ft: Meta[T]) {
      def map[T1](f: T => T1): Meta[T1] = ft.copy(value = ft.value.map(f))
    }
  }
}

/** Specialised typeclass to expose operations on higher kinded data (case-classes where each field is wrapped in F[_])
  */
trait HKD[Data[_[_]]] {

  // allows to instantiate the case class
  def pure[A[_]](f: [t] => () => A[t]): Data[A]

  extension [A[_]](af: Data[A]) {
    // allows to map over case class fields
    def mapK[B[_]](f: A ~> B)(using Functor[B]): Data[B]
    def mapK1[B[_]](f: A ~> B)(using Functor[A]): Data[B]
  }

  def map2[A[_], B[_], C[_]](dataA: Data[A], dataB: Data[B])(f: [t] => (A[t], B[t]) => C[t])(using Extract[A], Extract[B], Functor[A], Functor[B]): Data[C]

  private[HKD] def meta(index: Int, name: String): Data[Meta]
  lazy val meta: Data[Meta] = meta(0, "")
  def indices: Data[Const[Int]]
  def fieldNames: IndexedSeq[String]

}

object HKD {

  trait FieldUtils[F[_[_]], T] {
    def extract[A[_]](in: F[A]): A[T]
    def name: String
  }

  def apply[F[_[_]]](using hkd: HKD[F]): HKD[F] = hkd

  // special instance for simple types, where F = K11.Id[T], and so F[A] = T
  given id[T]: HKD[[f[_]] =>> f[T]] with {
    override def pure[A[_]](f: [t] => () => A[t]): A[T] = f[T]()

    extension [A[_]](at: A[T]) {
      def mapK[B[_]](f: A ~> B)(using Functor[B]): B[T]  = f(at)
      def mapK1[B[_]](f: A ~> B)(using Functor[A]): B[T] = f(at)
    }

    def map2[A[_], B[_], C[_]](dataA: A[T], dataB: B[T])(f: [t] => (A[t], B[t]) => C[t])(using Extract[A], Extract[B], Functor[A], Functor[B]): C[T] =
      f[T](dataA, dataB)

    def meta(index: Int, name: String): Meta[T] = Meta(index, name, None)
    def fieldNames: IndexedSeq[String]          = Vector("<self>")

    override def indices: Id[T][Const[Int]] = 0
  }

  given wrappedData[Data[_[_]]](using dHKD: HKD[Data]): HKD[[f[_]] =>> f[Data[f]]] with {
    override def pure[A[_]](f: [t] => () => A[t]): A[Data[A]] = f[Data[A]]()
    extension [A[_]](af: A[Data[A]]) {
      def mapK[B[_]](f: A ~> B)(using Functor[B]): B[Data[B]]  = f(af).map(_.mapK(f))
      def mapK1[B[_]](f: A ~> B)(using Functor[A]): B[Data[B]] = f(af.map(_.mapK1(f)))
    }

    def map2[A[_], B[_], C[_]](aDataA: A[Data[A]], bDataB: B[Data[B]])(f: [t] => (A[t], B[t]) => C[t])(using Extract[A], Extract[B], Functor[A], Functor[B]): C[Data[C]] = {
//      aDataA.map((dataA: Data[A]) => bDataB.map((dataB: Data[B]) => dHKD.map2(dataA, dataB)(f)))
      val dataC = dHKD.map2(aDataA.extract, bDataB.extract)(f)
      val aDataC = aDataA.map(_ => dataC)
      val bDataC = bDataB.map(_ => dataC)
      f(aDataC, bDataC)
    }

    def meta(index: Int, name: String): Meta[Data[Meta]] = Meta(index, name, Some(dHKD.meta(index, name)))
    override def indices: Const[Int][Data[Const[Int]]]   = 0
    override def fieldNames: IndexedSeq[String]          = Vector()
  }

  given shapeGen[Data[_[_]]](using inst: => K11.ProductInstances[HKD, Data], labelling: Labelling[Data[Const[Any]]]): HKD[Data] with {
    override def pure[A[_]](f: [t] => () => A[t]): Data[A] = inst.construct([t[_[_]]] => (p: HKD[t]) => p.pure([t1] => () => f[t1]()))

    extension [A[_]](dataA: Data[A]) {
      def mapK[B[_]](f: A ~> B)(using Functor[B]): Data[B]  =
        inst.map(dataA)([t[_[_]]] => (ft: HKD[t], ta: t[A]) => ft.mapK(ta)(f))
      def mapK1[B[_]](f: A ~> B)(using Functor[A]): Data[B] =
        inst.map(dataA)([t[_[_]]] => (ft: HKD[t], ta: t[A]) => ft.mapK1(ta)(f))
    }

    def map2[A[_], B[_], C[_]](dataA: Data[A], dataB: Data[B])(f: [t] => (A[t], B[t]) => C[t])(using Extract[A], Extract[B], Functor[A], Functor[B]): Data[C] = {
      inst.map2[A, B, C](dataA, dataB)([Data1[_[_]]] => (p: HKD[Data1], data1A: Data1[A], data1B: Data1[B]) => p.map2(data1A, data1B)(f))
    }

    def meta(index: Int, name: String): Data[Meta] = {
      var index = 0;
      inst.construct(
        [Data1[_[_]]] =>
          (p: HKD[Data1]) =>
            {
              val idx = index;
              index += 1;
              p.meta(idx, labelling.elemLabels(idx))
            },
      )
    }

    def fieldNames: IndexedSeq[String] = labelling.elemLabels

    override def indices: Data[Const[Int]] = {
      var index = 0
      inst.construct(
        [t[_[_]]] =>
          (p: HKD[t]) =>
            p.pure(
              [t1] =>
                () =>
                  {
                    val i = index
                    index += 1;
                    i
                  },
            ),
      )
    }
  }

  inline def derived[F[_[_]]](using K11.ProductGeneric[F], Labelling[F[Const[Any]]]): HKD[F] = shapeGen

  extension [F[_[_]]](hkd: HKD[F]) {
    def construct[A[_]](f: [t] => FieldUtils[F, t] => A[t]): F[A] = {
      given HKD[F] = hkd
      class FieldUtilsImpl[T](idx: Int) extends FieldUtils[F, T] {
        override def extract[A[_]](in: F[A]): A[T] = {
          val allValues: Vector[Any] = HKDUtils.collectFields(values(in))
          allValues(idx).asInstanceOf[A[T]]
        }
        override val name: String                  = hkd.fieldNames(idx)
      }
      hkd.indices.mapK1([t] => idx => f[t](new FieldUtilsImpl[t](idx)))
    }
  }

  def map2[F[_[_]]: HKD, A[_], B[_], C[_]](fa: F[A], fb: F[B])(func: [t] => (A[t], B[t]) => C[t]): F[C] = {
    HKD[F].construct([t] => (fu: FieldUtils[F, t]) => func[t](fu.extract(fa), fu.extract(fb)))
  }

  def indexes[F[_[_]]](using hkd: HKD[F]): F[Const[Int]]                = {
    var index = 0;
    hkd.pure(
      [t] =>
        () =>
          {
            val i = index
            index += 1;
            i
          },
    )
  }
  def typedNames[F[_[_]]](using hkd: HKD[F]): F[Const[String]]          = {
    indexes[F].mapK([t] => idx => hkd.fieldNames(idx))
  }
  def values[F[_[_]], A[_]](fa: F[A])(using hkd: HKD[F]): F[Const[Any]] = {
    fa.mapK[Const[Any]]([t] => (x: A[t]) => x: Any)
  }

}
