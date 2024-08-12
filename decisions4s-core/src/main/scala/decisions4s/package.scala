import decisions4s.exprs.Literal

package object decisions4s {

  export LiteralShow.showAsLiteral
  export exprs.Function

  type Value[T] = T

  opaque type OutputValue[T] <: Expr[T] = Expr[T]
  object OutputValue {
    implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
    implicit def fromExpr[T](expr: Expr[T]): OutputValue[T]               = expr
  }

  def wholeInput[In[_[_]]](using ec: EvaluationContext[In]): In[Expr] = ec.wholeInput

  extension [T](value: T) {
    def asLiteral(using LiteralShow[T]): Expr[T] = Literal(value)
  }

}
