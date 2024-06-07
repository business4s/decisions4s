package decisions4s.example.docs

import decisions4s.HKD

object HitPoliciesExample {

  case class In[F[_]]() derives HKD
  case class Out[F[_]]() derives HKD

  import decisions4s.*
  val in: In[Value]              = ???
  val rules: List[Rule[In, Out]] = ???
  val name: String               = ???

  // start_single
  val single: DecisionTable[In, Out, HitPolicy.Single] = DecisionTable(rules, name, HitPolicy.Single)
  single.evaluateSingle(in)
  // end_single

  // start_distinct
  val distinct: DecisionTable[In, Out, HitPolicy.Distinct] = DecisionTable(rules, name, HitPolicy.Distinct)
  distinct.evaluateDistinct(in)
  // end_distinct

  // start_first
  val first: DecisionTable[In, Out, HitPolicy.First] = DecisionTable(rules, name, HitPolicy.First)
  first.evaluateFirst(in)
  // end_first

  // start_collect
  val collect: DecisionTable[In, Out, HitPolicy.Collect] = DecisionTable(rules, name, HitPolicy.Collect)
  collect.evaluateCollect(in)
// end_collect

  // start_sum
  val collectSum: DecisionTable[In, Out, HitPolicy.CollectSum] = DecisionTable(rules, name, HitPolicy.CollectSum)
  val merge: (Out[Value], Out[Value]) => Out[Value]            = ???
  collectSum.evaluateSum(in)(merge)
  // end_sum

  {
    // start_min
    val collectMin: DecisionTable[In, Out, HitPolicy.CollectMin] = DecisionTable(rules, name, HitPolicy.CollectMin)
    given Ordering[Out[Value]]                                   = ???
    collectMin.evaluateMin(in)
    // end_min
  }

  {
    // start_max
    val collectMax: DecisionTable[In, Out, HitPolicy.CollectMax] = DecisionTable(rules, name, HitPolicy.CollectMax)
    given Ordering[Out[Value]]                                   = ???
    collectMax.evaluateMax(in)
    // end_max
  }

  // start_count
  val collectCount: DecisionTable[In, Out, HitPolicy.CollectCount] = DecisionTable(rules, name, HitPolicy.CollectCount)
  collectCount.evaluateCount(in)
  // end_count

}
