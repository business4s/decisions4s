---
sidebar_position: 3
---

# Expressions

`Decisions4s` uses custom expressions for defining the matching logic for a given rule. Expression is just an object
that can produce given `Out` based on `In` and statically render its string representation.

```scala 
trait Expr[-In, +Out] {
  def evaluate(in: In): Out

  def renderExpression: String
}
```

There is also a specialised type `UnaryTest[In] extends Expr[In, Boolean]` that allows us to closely follow
the [FEEL model](https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-unary-tests/). This could be
considered an internal complexity of the library, but all the rules have to be of type `UnaryTest[T]`.

<!-- @formatter:off -->
```scala
case class Rule[Input[_[_]], Output[_[_]]](
  matching: Input[UnaryTest],
  ...
)
```
<!-- @formatter:on -->

Implicit conversions between `Expr` and `UnaryTest` are provided based on the rules below.

> A unary-tests expression returns true if one of the following conditions is fulfilled:
>
> - The expression evaluates to true when the input value is applied to it.
> - The expression evaluates to a list, and the input value is equal to at least one of the values in that list.
> - The expression evaluates to a value, and the input value is equal to that value.
> - The expression is equal to - (a dash).

## Built-in Expressions

`Decisions4s` provides basic numeric and boolean expressions that can be used by invoking methods on `it` object or on
expressions themselves.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_expr end=end_expr
```

## Custom Expressions

To define a custom expression its enough to extend `Expr` trait.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_custom_generic end=end_custom_generic
```

The same can be simplified if it needs to be applied only to the input of the rule, not to any expression.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_custom_simplified end=end_custom_simplified
```

The downside is that rendered form might not be clear to the reader

## FEEL Compatibility

The expressions provided by library guarantee compatibility
with [FEEL](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/). This means their rendered form, when
evaluated, yields the same result as direct evaluation. This guarantee is provided to lower the mental load, so that we
can rely on a properly specified format instead of defining our own. Having said that, it's important to remember that
**rendered form is not intended to be evaluated**. Decisions4s will use direct evaluation when evaluating decision
tables.

User-defined expressions don't have to keep FEEL compatibility.
