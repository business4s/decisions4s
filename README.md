# Decisions4s

![Discord](https://img.shields.io/discord/1240565362601230367?style=flat-square&logo=discord&link=https%3A//bit.ly/business4s-discord)

This repository contains a prototype of library helping in expressing business rules. It can be seen as an alternative
to Rules Engines or DMN.

The approach taken relies on higher-order data and follows DMN evaluation model, where each decision has specific
input & output types and consists of a set of rules that match on the input and produce the output.

## Getting Started

> [!NOTE]
> As of now Decisions4s don't have a single release. The example below assumes you released it yourself or you're
> contributing to the examples project.

We will model rules governing a pull request process. We start by defining the input and out of the decision.

```scala 3
import decisions4s.*

case class Input[F[_]](numOfApprovals: F[Int], isTargetBranchProtected: F[Boolean], authorIsAdmin: F[Boolean])
  derives HKD

case class Output[F[_]](allowMerging: F[Boolean], notifyUnusualAction: F[Boolean])
  derives HKD
```

We take 3 values as input and provide 2 values as the output. Now let's define the rules

```scala 3
def rules: List[Rule[Input, Output]] = List(
  Rule(
    matching = Input(
      numOfApprovals = it > 0,
      isTargetBranchProtected = it.isFalse,
      authorIsAdmin = it.catchAll,
    ),
    output = Output(
      allowMerging = true,
      notifyUnusualAction = false,
    ),
  ),
  Rule(
    matching = Input(
      numOfApprovals = it > 1,
      isTargetBranchProtected = it.isTrue,
      authorIsAdmin = it.catchAll,
    ),
    output = Output(
      allowMerging = true,
      notifyUnusualAction = false,
    ),
  ),
  Rule(
    matching = Input(
      numOfApprovals = it.catchAll,
      isTargetBranchProtected = it.catchAll,
      authorIsAdmin = it.isTrue,
    ),
    output = Output(
      allowMerging = true,
      notifyUnusualAction = true,
    ),
  ),
  Rule.default(
    Output(
      allowMerging = false,
      notifyUnusualAction = false,
    ),
  ),
)
```

We have defined 4 rules:

* unprotected branch requires 1 approval
* protected branch requires 2 approvals
* admin can merge anything without approvals but this sends a notification
* nothing can be merged otherwise

Now let's create a decision table

```scala 3
val decisionTable: DecisionTable[Input, Output, HitPolicy.Unique] =
  DecisionTable(
    rules,
    inputNames = Name.auto[Input],
    outputNames = Name.auto[Output],
    name = "PullRequestDecision",
    hitPolicy = HitPolicy.Unique
  )
```

Defining the decision means specifying the rules and names for fields and decision itself.

Now we can evaluate our decision:

```scala 3
decisionTable.evaluateUnqiue(Input[Value](
  numOfApprovals = 1,
  isTargetBranchProtected = false,
  authorIsAdmin = true
))
// Output(allowMerging = true, notifyUnusualAction = false)
```

It works! Lets generate the DMN for the business.

```scala 3
import decisions4s.dmn.DmnConverter

val dmnInstance = DmnConverter.convert(decisionTable)
import org.camunda.bpm.model.dmn.Dmn

Dmn.writeModelToFile(new java.io.File(s"./${decisionTable.name}.dmn"), dmnInstance)
```

Now if we open this file in [bpmn.io](https://bpmn.io/toolkit/dmn-js/) or Camunda Modeler we will see the following
table.

![PullRequestDecision.png](docs/PullRequestDecision.png)

To see the full example,
check [PullRequestDecision.scala](decisions4s-examples/src/main/scala/decisions4s/example/docs/PullRequestDecision.scala)

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
