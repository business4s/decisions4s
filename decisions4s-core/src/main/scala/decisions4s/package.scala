import decisions4s.exprs.{Literal, UnaryTest}

package object decisions4s {

  export LiteralShow.showAsLiteral

  type ~>[A[_], B[_]]      = [t] => A[t] => B[t]
  type Tuple2K[A[_], B[_]] = [t] =>> (A[t], B[t])

  type Value[T]       = T
  type Description[T] = String

  type MatchingExpr[In[_[_]]] = [T] =>> EvaluationContext[In] ?=> UnaryTest[T]



  type OutputExpr[In[_[_]]]             = [T] =>> EvaluationContext[In] ?=> OutputValue[T]
  opaque type OutputValue[T] <: Expr[T] = Expr[T]
  object OutputValue {
    implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
    implicit def fromExpr[T](expr: Expr[T]): OutputValue[T]               = expr
  }

  def wholeInput[In[_[_]]](using ec: EvaluationContext[In]): In[Expr] = ec.wholeInput

  extension [T](value: T){
    def asLiteral(using LiteralShow[T]): Expr[T] = Literal(value)
  }

}
