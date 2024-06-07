import decisions4s.exprs.{Literal, UnaryTest}
import shapeless3.deriving.{K0, Labelling}

package object decisions4s {

  export DecisionTable.HitPolicy

  type ~>[A[_], B[_]]      = [t] => A[t] => B[t]
  type Tuple2K[A[_], B[_]] = [t] =>> (A[t], B[t])

  type Value[T]       = T
  type Description[T] = String

  type ValueExpr[T]    = Expr[Any, T]
  type MatchingExpr[T] = UnaryTest[T]

  opaque type OutputValue[T] <: Expr[Any, T] = Expr[Any, T]
  object OutputValue {
    implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
  }

}
