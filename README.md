# Decisions4s

![Discord](https://img.shields.io/discord/1240565362601230367?style=flat-square&logo=discord&link=https%3A//bit.ly/business4s-discord)

This repository contains a prototype of library helping in expressing business rules. It can be seen as an alternative
to Rules Engines or DMN.

The approach taken relies on higher-order data and follows DMN evaluation model, where each decision has specific
input & output types and consists of a set of rules that match on the input and produce the output.

## Expressions coverage and FEEL compatibility

As of now, Decisions4s supports a limited set of expressions, mostly focusing on boolean logic and basic tests. Missing
expressions need to be provided at the use-site.

The expressions provide guarantee compatibility
with [FEEL](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/). This means their rendered form, when
evaluated, yields the same result as direct evaluation. This guarantee is provided to lower the mental load, so that we
can rely on a properly specified format instead of defining our own. Having said that, it's important to remember that
**rendered form is not intended to be evaluated**; Decisions4s will use direct evaluation when evaluating decision
tables.

User-defined expressions dont have to keep FEEL compatibility.

## Effectful evaluation

Decisions4s allow to evaluate a decision table based on effectful inputs
(e.g. when network call is required for particular piece of data).

* All inputs will be memoized and executed only if required by the rule.
* Input is considered not required only if `catchAll` expression is used for matching.
* table need to use HitPolicy.First (because it's the only one for which lazy evaluation is expected)

```
import cats.effect.IO
import decisions4s.*
import decisions4s.cats.effect.given

val decisionTable: DecisionTable[Input, Output, HitPolicy.First] = ???
val input: Input[IO] = ???

decisionTable.evaluateFirstF(input)
```

## Non-features

The following items are currently not available, although they could be implemented if there is enough interest.

* **Serialization** - currently the decisions are not meant to be serialized and transferred over the wire, but it
  should be possible.
