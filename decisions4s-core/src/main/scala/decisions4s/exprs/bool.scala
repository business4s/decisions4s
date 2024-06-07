package decisions4s.exprs

import decisions4s.Expr

import scala.math.Ordered.orderingToOrdered

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-boolean-expressions/

case class Equal[I, O](a: Expr[I, O], b: Expr[I, O]) extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) == b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} = ${b.renderExpression}"
}

case class NotEqual[I, O](a: Expr[I, O], b: Expr[I, O]) extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) != b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} != ${b.renderExpression}"
}

case class LessThan[I, O: Ordering](a: Expr[I, O], b: Expr[I, O])                                   extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) < b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} < ${b.renderExpression}"
}
case class LessThanEqual[I, O: Ordering](a: Expr[I, O], b: Expr[I, O])                              extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) <= b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} <= ${b.renderExpression}"
}
case class GreaterThan[I, O: Ordering](a: Expr[I, O], b: Expr[I, O])                                 extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) > b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} > ${b.renderExpression}"
}
case class GreaterThanEqual[I, O: Ordering](a: Expr[I, O], b: Expr[I, O])                            extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = a.evaluate(in) >= b.evaluate(in)
  override def renderExpression: String = s"${a.renderExpression} >= ${b.renderExpression}"
}
case class Between[I, O: Ordering](arg: Expr[I, O], lowerBound: Expr[I, O], upperBound: Expr[I, O]) extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = {
    val argValue = arg.evaluate(in)
    argValue >= lowerBound.evaluate(in) && argValue <= upperBound.evaluate(in)
  }
  override def renderExpression: String =
    s"${arg.renderExpression} between ${lowerBound.renderExpression} and ${upperBound.renderExpression}"
}

case object True extends Expr[Any, Boolean] {
  override def evaluate(in: Any): Boolean   = true
  override def renderExpression: String = "true"
}

case object False extends Expr[Any, Boolean] {
  override def evaluate(in: Any): Boolean   = false
  override def renderExpression: String = "false"
}

case class And[I](lhs: Expr[I, Boolean], rhs: Expr[I, Boolean]) extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = lhs.evaluate(in) && rhs.evaluate(in)
  override def renderExpression: String = s"${lhs.renderExpression} and ${rhs.renderExpression}"
}

case class Or[I](lhs: Expr[I, Boolean], rhs: Expr[I, Boolean])  extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = lhs.evaluate(in) || rhs.evaluate(in)
  override def renderExpression: String = s"${lhs.renderExpression} or ${rhs.renderExpression}"
}

case class In[I, O](lhs: Expr[I, O], rhs: UnaryTest[O]) extends Expr[I, Boolean] {
  override def evaluate(in: I): Boolean     = rhs.evaluate(lhs.evaluate(in))
  override def renderExpression: String = s"${lhs.renderExpression} in ${rhs.renderExpression}"
}
