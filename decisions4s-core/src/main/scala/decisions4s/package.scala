
package object decisions4s {

  type Value[T] = T
  type Description[T] = String
  
  type ValueExpr[T] = Expr[T]
  type MatchingExpr[T] = ValueExpr[T] => Expr[Boolean]
  
}
