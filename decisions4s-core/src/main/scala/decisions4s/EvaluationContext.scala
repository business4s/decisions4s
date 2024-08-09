package decisions4s

import decisions4s.exprs.VariableStub

trait EvaluationContext[In[_[_]]] {
  def wholeInput: In[Expr]
}

object EvaluationContext {

  def stub[Data[_[_]]: HKD]: EvaluationContext[Data] = {
    new EvaluationContext[Data] {
      override val wholeInput: Data[Expr] = HKD[Data].meta.mapK1([t] => meta => VariableStub[t](meta.name))
    }
  }

}
