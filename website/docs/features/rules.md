---
sidebar_position: 3
---

# Writing Rules

`Decisions4s` uses custom expressions for defining the rules.
Each rule has two parts: matching on the input and producing the output, both of which are defined using
expressions.

Expression is just an object that can produce given `Out` and render its string representation.

```scala 
trait Expr[+Out] {
  def evaluate: Out

  def renderExpression: String
}
```

There is also a specialized type `UnaryTest[In]` that allows us to loosely follow
the [FEEL model](https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-unary-tests/). This could be
considered an internal complexity of the library, but all the matching logic has to be of type `UnaryTest[T]`.

<!-- @formatter:off -->
```scala
case class Rule[Input[_[_]], Output[_[_]]](
  matching: Input[UnaryTest],
  ...
)
```
<!-- @formatter:on -->

All the most common ways of building `UnaryTest`s are accessible through `it` object.
Implicit conversion between `Expr[Boolean]` is also defined.

## Built-in Expressions

`Decisions4s` provides basic numeric and boolean expressions that can be used by invoking methods on `it` object or on
expressions themselves.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_expr end=end_expr
```

## Custom Expressions

To define a custom expression its enough to extend `Expr` trait.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_custom_generic end=end_custom_generic
```

## FEEL Compatibility

The expressions provided by the library guarantee compatibility
with [FEEL](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/). This means their rendered form, when
evaluated, yields the same result as direct evaluation. This guarantee is provided to lower the mental load so that we
can rely on a properly specified format instead of defining our own. Having said that, it's important to remember that
**rendered form is not intended to be evaluated**. Decisions4s will use direct evaluation when evaluating decision
tables.

User-defined expressions don't have to keep FEEL compatibility.

## Accessing Other Inputs

By default, all matching logic operates on the input it is defined for.
To access other pieces of input one can use `wholeInput` method.
The same way can be used to build the output value based on inputs.
The example below compares `a` with `b` and returns their sum if they are equal.

```scala file=./main/scala/decisions4s/example/docs/RulesExample.scala start=start_whole_input end=end_whole_input
```

## Using Data Structures As Inputs And Outputs

:::warning
This feature is experimental and might come with significantly rough edges around its API.
:::

For more complicated decisions, it might be useful to group inputs or outputs into dedicated objects.
Decisions4s supports this scenario through nested higher kinded data structures.

```scala file=./main/scala/decisions4s/example/docs/RulesExample.scala start=start_nested_structures end=end_nested_structures
```

As of now, matching has to be done through `wholeInput` and no method of `it` will work.

Nested data structures will be rendered
as [FEEL Context Expressions](https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-context-expressions/). 
