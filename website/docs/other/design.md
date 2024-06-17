---
sidebar_position: 1
---

# Design

## DMN

Decisions4s is heavily inspired
by [Decision Model and Notation](https://en.wikipedia.org/wiki/Decision_Model_and_Notation) (DMN)
which is an open standard developed by business analysis community in 2015 and refined since then.

DMN was designed as a model-first solution, where you define the rules in the model and execute that model. Decisions4s
reverse that approach, rules are defined as code and model is generated from that. This aims at making it much more
developer friendly while keeping the benefits of the standard.

DMN allows modeling single decisions as well as more complex decision trees.
`Decisions4s` limits itself to single decisions for now, as supporting decision trees would multiply the library
complexity. This could be explored in future versions of the library.

## Higher kinded data

Decisions4s is build around higher-kinded data - a pattern where case class fields are wrapped in the higher-kinded
type `F[_]`. To facilitate generic operations on such data structures, we use a custom `HKD` typeclass. This type class
is a merge of few more principled typeclases, such as `FunctorK` and `SemigrupalK`. We intentionally don't use those
concepts for a couple of reasons

* `HKD` is a single typeclass, lowering general complexity of the library
* `HKD` is specialised for case classes (doesn't support sealed traits), allowing us to expose more operations
* `HKD` is based on scala 3 primitives, which might be hard to achieve in libraries such as `cats-tagless` 