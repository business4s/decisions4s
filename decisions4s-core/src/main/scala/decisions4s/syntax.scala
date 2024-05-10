package decisions4s

import decisions4s.exprs.*

object syntax {
  def catchAll[T]: MatchingExpr[T] = True

  extension [T](v: T) {
    def asLiteral(using show: LiteralShow[T]): ValueExpr[T]     = Literal(v)
    def matchEqual(using show: LiteralShow[T]): MatchingExpr[T] = Equal(Literal(v))
  }
  extension [T](v: List[T]) {
    def matchAny(using show: LiteralShow[T]): MatchingExpr[T] = Or(v.map(_.matchEqual))
  }
}
