package decisions4s

import decisions4s.internal.HKDUtils
import shapeless3.deriving.K11.Id
import shapeless3.deriving.{Const, K11, Labelling, ~>}

trait Functor[F[_]] {
  extension [T](ft: F[T]) {
    def map[T1](f: T => T1): F[T1]
  }
}

/** Specialised typeclass to expose operations on higher kinded data (case-classes where each field is wrapped in F[_])
  */
trait HKD[F[_[_]]] {

  // allows to instantiate the case class
  def pure[A[_]](f: [t] => () => A[t]): F[A]

  extension [A[_]](af: F[A]) {
    // allows to map over case class fields
    def mapK[B[_]](f: A ~> B): F[B]

  }
  def indices: F[Const[Int]]
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
      def mapK[B[_]](f: A ~> B): B[T] = f(at)
    }

    def fieldNames: IndexedSeq[String] = Vector("<self>")

    override def indices: Id[T][Const[Int]] = 0
  }

//  type Id[t[_]] = [f[_]] =>> f[t]
//  given id1Gen[F[_]]: HKD[Id[F]] = ???

//  given idf[Data[_[_]]](using dHKD: HKD[Data]): HKD[[f[_]] =>> f[Data[f]]] with {
//    override def pure[A[_]](f: [t] => () => A[t]): A[Data[A]] = f[Data[A]]()
//    extension [A[_]](af: A[Data[A]]) {
//      def mapK[B[_]](f: A ~> B): B[Data[B]] = dHKD.mapK()
//    }
//    override def indices: Const[Int][Data[Const[Int]]]        = 0
//    override def fieldNames: IndexedSeq[String]               = Vector()
//  }

  given shapeGen[F[_[_]]](using inst: => K11.ProductInstances[HKD, F], labelling: Labelling[F[Const[Any]]]): HKD[F] with {
    override def pure[A[_]](f: [t] => () => A[t]): F[A] = inst.construct([t[_[_]]] => (p: HKD[t]) => p.pure([t1] => () => f[t1]()))

    extension [A[_]](fa: F[A]) {
      def mapK[B[_]](f: A ~> B): F[B] =
        inst.map(fa)([t[_[_]]] => (ft: HKD[t], ta: t[A]) => ft.mapK(ta)(f))
    }

    def fieldNames: IndexedSeq[String] = labelling.elemLabels

    override def indices: F[Const[Int]] = {
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

//  extension [F[_[_]], A[_]](fa: F[A])(using hkd: HKD[F]) {
//
//    // allows to merge case classes together
////    def productK[B[_]](fb: F[B]): F[Tuple2K[A, B]] =
//
//  }

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
      hkd.indices.mapK([t] => idx => f[t](new FieldUtilsImpl[t](idx)))
    }
  }

  def map2[F[_[_]]: HKD, A[_], B[_], C[_]](fa: F[A], fb: F[B])(func: [t] => (A[t], B[t]) => C[t]): F[C] = {
    summon[HKD[F]].construct([t] => (fu: FieldUtils[F, t]) => func[t](fu.extract(fa), fu.extract(fb)))
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
