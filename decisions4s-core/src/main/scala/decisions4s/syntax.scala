package decisions4s

import decisions4s.exprs.*

object syntax {
  def catchAll[T]: MatchingExpr[T] = _ => True

  extension [T](v: T) {
    def toValueExpr: ValueExpr[T]       = Literal(v)
    def toMatchingExpr: MatchingExpr[T] = Equal(_, Literal(v))
  }
}
