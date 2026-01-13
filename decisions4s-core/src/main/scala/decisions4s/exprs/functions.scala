package decisions4s.exprs

import decisions4s.Expr

object Function {

  def apply[Out](name: String)                    = new Builder[Out](name)
  def autoNamed[Out](using name: sourcecode.Name) = new Builder[Out](name.value)

  class Builder[Out](name: String) {

    def apply[A](arg: Expr[A])(logic: A => Out): Expr[Out] = new Function1[A, Out](arg, name) {
      override def evaluate(a: A): Out = logic(a)
    }

    def apply[A, B](arg1: Expr[A], arg2: Expr[B])(logic: (A, B) => Out): Expr[Out] = new Function2[A, B, Out](arg1, arg2, name) {
      override def evaluate(a: A, b: B): Out = logic(a, b)
    }

    def apply[A, B, C](arg1: Expr[A], arg2: Expr[B], arg3: Expr[C])(logic: (A, B, C) => Out): Expr[Out] =
      new Function3[A, B, C, Out](arg1, arg2, arg3, name) {
        override def evaluate(a: A, b: B, c: C): Out = logic(a, b, c)
      }
  }
}

trait Function1[A, Out](arg1Param: Expr[A], nameParam: String) extends Expr[Out] {
  val arg1 = arg1Param
  val name = nameParam
  def evaluate(a: A): Out

  override def evaluate: Out            = evaluate(arg1.evaluate)
  override def renderExpression: String = s"${name}(${arg1.renderExpression})"
}

trait Function2[A, B, Out](arg1Param: Expr[A], arg2Param: Expr[B], nameParam: String) extends Expr[Out] {
  val arg1 = arg1Param
  val arg2 = arg2Param
  val name = nameParam
  def evaluate(a: A, b: B): Out

  override def evaluate: Out            = evaluate(arg1.evaluate, arg2.evaluate)
  override def renderExpression: String = s"${name}(${arg1.renderExpression}, ${arg2.renderExpression})"
}

trait Function3[A, B, C, Out](arg1Param: Expr[A], arg2Param: Expr[B], arg3Param: Expr[C], nameParam: String) extends Expr[Out] {
  val arg1 = arg1Param
  val arg2 = arg2Param
  val arg3 = arg3Param
  val name = nameParam
  def evaluate(a: A, b: B, c: C): Out

  override def evaluate: Out            = evaluate(arg1.evaluate, arg2.evaluate, arg3.evaluate)
  override def renderExpression: String = s"${name}(${arg1.renderExpression}, ${arg2.renderExpression}, ${arg3.renderExpression})"
}
