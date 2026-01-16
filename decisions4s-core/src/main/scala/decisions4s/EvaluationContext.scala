package decisions4s

import decisions4s.exprs.{Variable, VariableStub}

trait EvaluationContext[In[_[_]]] {
  def wholeInput: In[Expr]
}

object EvaluationContext {
  
  def fromInput[Data[_[_]]: HKD](in: Data[Value]): EvaluationContext[Data] = {
    new EvaluationContext[Data] {
      override val wholeInput: Data[Expr] = HKD[Data].map2(in, HKD[Data].meta)([t] => (value, meta) => Variable[t](meta.name, value))
    }
  }
  
  def stub[Data[_[_]]: HKD]: EvaluationContext[Data] = {
    new EvaluationContext[Data] {
      override val wholeInput: Data[Expr] = HKD[Data].meta.mapK1([t] => meta => VariableStub[t](meta.name))
    }
  }

}
