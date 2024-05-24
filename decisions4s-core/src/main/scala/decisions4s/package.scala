import decisions4s.exprs.Literal
import shapeless3.deriving.{K0, Labelling}

import scala.annotation.implicitNotFound

package object decisions4s {

  type ~>[A[_], B[_]]      = [t] => A[t] => B[t]
  type Tuple2K[A[_], B[_]] = [t] =>> (A[t], B[t])

  type Value[T]       = T
  type Description[T] = String

  type ValueExpr[T]    = Expr[Any, T]
  type MatchingExpr[T] = Expr[T, Boolean]

  opaque type OutputValue[T] <: Expr[Any, T] = Expr[Any, T]
  object OutputValue {
    implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
  }

  type Name[T] = String
  object Name {
    def apply[T](value: String): Name[T] = value

    type IsName[T] = Name[?] =:= T
    inline def auto[F[_[_]]](using pInst: K0.ProductInstances[IsName, F[Name]], labelling: Labelling[F[Name]]): F[Name] = {
      type Acc = Seq[String]
      pInst
        .unfold[Acc](labelling.elemLabels)(
          [t] =>
            (acc: Acc, isName: IsName[t]) =>
              {
                acc.tail -> Some(isName.apply(Name(acc.head)))
              },
        )
        ._2
        .get
    }

  }

}
