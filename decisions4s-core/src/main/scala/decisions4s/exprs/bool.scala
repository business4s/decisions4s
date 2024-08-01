package decisions4s.exprs

import decisions4s.Expr

import scala.math.Ordered.orderingToOrdered

// https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-boolean-expressions/

case class Equal[O](a: Expr[O], b: Expr[O]) extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate == b.evaluate
  override def renderExpression: String = s"${a.renderExpression} = ${b.renderExpression}"
}

case class NotEqual[O](a: Expr[O], b: Expr[O]) extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate != b.evaluate
  override def renderExpression: String = s"${a.renderExpression} != ${b.renderExpression}"
}

case class LessThan[O: Ordering](a: Expr[O], b: Expr[O])                                extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate < b.evaluate
  override def renderExpression: String = s"${a.renderExpression} < ${b.renderExpression}"
}
case class LessThanEqual[O: Ordering](a: Expr[O], b: Expr[O])                           extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate <= b.evaluate
  override def renderExpression: String = s"${a.renderExpression} <= ${b.renderExpression}"
}
case class GreaterThan[O: Ordering](a: Expr[O], b: Expr[O])                             extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate > b.evaluate
  override def renderExpression: String = s"${a.renderExpression} > ${b.renderExpression}"
}
case class GreaterThanEqual[O: Ordering](a: Expr[O], b: Expr[O])                        extends Expr[Boolean] {
  override def evaluate: Boolean        = a.evaluate >= b.evaluate
  override def renderExpression: String = s"${a.renderExpression} >= ${b.renderExpression}"
}
case class Between[O: Ordering](arg: Expr[O], lowerBound: Expr[O], upperBound: Expr[O]) extends Expr[Boolean] {
  override def evaluate: Boolean        = {
    val argValue = arg.evaluate
    argValue >= lowerBound.evaluate && argValue <= upperBound.evaluate
  }
  override def renderExpression: String =
    s"${arg.renderExpression} between ${lowerBound.renderExpression} and ${upperBound.renderExpression}"
}

case object True extends Expr[Boolean] {
  override def evaluate: Boolean        = true
  override def renderExpression: String = "true"
}

case object False extends Expr[Boolean] {
  override def evaluate: Boolean        = false
  override def renderExpression: String = "false"
}

case class Not(expr: Expr[Boolean]) extends Expr[Boolean] {
  override def evaluate: Boolean        = !expr.evaluate
  override def renderExpression: String = s"!${expr}"
}

case class And[I](lhs: Expr[Boolean], rhs: Expr[Boolean]) extends Expr[Boolean] {
  override def evaluate: Boolean        = lhs.evaluate && rhs.evaluate
  override def renderExpression: String = s"${lhs.renderExpression} and ${rhs.renderExpression}"
}

case class Or[I](lhs: Expr[Boolean], rhs: Expr[Boolean]) extends Expr[Boolean] {
  override def evaluate: Boolean        = lhs.evaluate || rhs.evaluate
  override def renderExpression: String = s"${lhs.renderExpression} or ${rhs.renderExpression}"
}

case class In[O](lhs: Expr[O], rhs: UnaryTest[O]) extends Expr[Boolean] {
  override def evaluate: Boolean        = rhs.evaluate(lhs.evaluate)
  override def renderExpression: String = s"${lhs.renderExpression} in ${rhs.renderExpression}"
}
