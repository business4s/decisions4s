import decisions4s.exprs.{Literal, UnaryTest}

package object decisions4s {

  export DecisionTable.HitPolicy

  type ~>[A[_], B[_]]      = [t] => A[t] => B[t]
  type Tuple2K[A[_], B[_]] = [t] =>> (A[t], B[t])

  type Value[T]       = T
  type Description[T] = String

  type ValueExpr[T]           = Expr[Any, T]
  type MatchingExpr[In[_[_]]] = [T] =>> EvaluationContext[In] ?=> UnaryTest[T]

  trait EvaluationContext[In[_[_]]] {
    def wholeInput: In[ValueExpr]
  }

  type OutputExpr[In[_[_]]]                  = [T] =>> EvaluationContext[In] ?=> OutputValue[T]
  opaque type OutputValue[T] <: Expr[Any, T] = Expr[Any, T]
  object OutputValue {
    implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
    implicit def fromExpr[T](expr: Expr[Any, T]): OutputValue[T]          = expr
  }

  def wholeInput[In[_[_]]](using ec: EvaluationContext[In]): In[ValueExpr] = ec.wholeInput

}
