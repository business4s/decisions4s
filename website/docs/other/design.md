---
sidebar_position: 1
---

# Design

## DMN

`Decisions4s` is heavily inspired
by [Decision Model and Notation](https://en.wikipedia.org/wiki/Decision_Model_and_Notation) (DMN), an open standard
developed by the business analysis community in 2015 and refined since then.

DMN was designed as a model-first solution, where you define the rules within the model and then execute that model.
`Decisions4s` reverses that approach — rules are defined in code, and the model is generated from that. This approach aims
to make the library more developer-friendly while retaining the benefits of the DMN standard.

DMN allows for modeling both single decisions and complex decision trees. However, `Decisions4s` currently focuses on
single decisions, as supporting decision trees would significantly increase the library's complexity. This feature may
be explored in future versions of the library.

## Higher-Kinded Data

`Decisions4s` is built around higher-kinded data — a pattern where case class fields are wrapped in the higher-kinded type
`F[_]`. To facilitate generic operations on such data structures, the library uses a custom `HKD` typeclass. This
typeclass merges several more principled typeclasses, such as `FunctorK` and `SemigroupalK`. We intentionally avoid
using these individual concepts for a few reasons:

* `HKD` is a single typeclass, which simplifies the overall complexity of the library.
* `HKD` is specialized for case classes (and doesn't support sealed traits), enabling us to expose more operations.
* `HKD` is based on Scala 3 primitives, which might be challenging to achieve in libraries like `cats-tagless`.
