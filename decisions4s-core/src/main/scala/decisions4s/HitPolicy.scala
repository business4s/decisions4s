package decisions4s

sealed trait HitPolicy
object HitPolicy {
  // Only a single rule is allowed to be satisfied. Matches to `Unique` in DNM.
  type Single = Single.type
  case object Single extends HitPolicy

  // Many rules can be satisfied, but they need to produce the same results. Matches to `Any` in DNM.
  type Distinct = Distinct.type
  case object Distinct extends HitPolicy

  // First satisfied rule produces the output
  type First = First.type
  case object First extends HitPolicy

  // Rule order is not supported as it brings no benefit over Collect. There is no reason to use different order than rule order.
  // type RuleOrder = RuleOrder.type
  // case object RuleOrder extends HitPolicy

  // Outputs are collected
  type Collect = Collect.type
  case object Collect extends HitPolicy

  // Outputs are summed together
  type CollectSum = CollectSum.type
  case object CollectSum extends HitPolicy

  // Minimum output is returned
  type CollectMin = CollectMin.type
  case object CollectMin extends HitPolicy

  // Maximum output is returned
  type CollectMax = CollectMax.type
  case object CollectMax extends HitPolicy

  // Number of satisfied rules is returned
  type CollectCount = CollectCount.type
  case object CollectCount extends HitPolicy
}
