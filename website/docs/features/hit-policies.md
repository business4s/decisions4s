---
sidebar_position: 4
---

# Hit Policies

`HitPolicy` defines how the output of the decision is computed. The possible pollicies are based
on [DMN Hit Policies](https://docs.camunda.org/manual/7.21/reference/dmn/decision-table/hit-policy/#collect-hit-policy)
with small changes in naming for increased clarity. Each HitPolicy comes with dedicated evaluation method and dedicated
return type.

When in doubt we recommend using `Single` policy as a default, as this is the most restrictive one.

### `HitPolicy.Single`
Allows for only one rule to be satisfied. Maps to DMN `Unique` policy.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_single end=end_single
```

### `HitPolicy.Distinct`
Allows for multiple rules to be satisfied as long as they provide the same output. Maps to DMN `Any` policy.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_distinct end=end_distinct
```

### `HitPolicy.First`
First satisfied rule defines the result, others are ignored.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_first end=end_first
```

### `HitPolicy.Collect`
Outputs are collected into a list.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_collect end=end_collect
```

### `HitPolicy.CollectSum`
Outputs are combined together.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_sum end=end_sum
```

### `HitPolicy.CollectMin`
Minimal output is returned.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_min end=end_min
```

### `HitPolicy.CollectMax`
Maximal output is returned.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_max end=end_max
```

### `HitPolicy.CollectCount`
Number of satisfied rules is returned.
```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_count end=end_count
```