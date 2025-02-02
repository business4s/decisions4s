---
sidebar_position: 4
---

# Hit Policies

`HitPolicy` defines how the output of a decision is computed from the results of particular rules. The available
policies are based
on [DMN Hit Policies](https://docs.camunda.org/manual/7.21/reference/dmn/decision-table/hit-policy/#collect-hit-policy)
with small changes in naming for increased clarity. Each `HitPolicy` comes with a dedicated evaluation method and return
type.

When in doubt, we recommend using the `Single` policy as a default, as it is the most restrictive one. Alternatively,
`First` is often the most intuitive and easy to reason about.

Each `HitPolicy` has a dedicated evaluation method, which returns a type specific to that policy.

### `HitPolicy.Single`

Allows for only one rule to be satisfied. Maps to the DMN `Unique` policy.

```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_single end=end_single
```

### `HitPolicy.Distinct`

Allows for multiple rules to be satisfied as long as they provide the same output. Maps to the DMN `Any` policy.

```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_distinct end=end_distinct
```

### `HitPolicy.First`

The first satisfied rule defines the result, and others are ignored.

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

The minimal output is returned.

```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_min end=end_min
```

### `HitPolicy.CollectMax`

The maximal output is returned.

```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_max end=end_max
```

### `HitPolicy.CollectCount`

The number of satisfied rules is returned.

```scala file=./main/scala/decisions4s/example/docs/HitPoliciesExample.scala start=start_count end=end_count
```