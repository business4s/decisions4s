---
sidebar_position: 3
---

# Writing Rules

Rules are classes that contain instance of `Input` filled with `UnaryTest`s and instance of `Output` filled
with `OutputValue`s.

<!-- @formatter:off -->
```scala
class Rule[Input[_[_]], Output[_[_]]](
  matching: Input[UnaryTest],
  output: Output[OutputValue]
)
```
<!-- @formatter:on -->

`UnaryTest` is a simple predicate that loosely follows
the [FEEL model](https://docs.camunda.io/docs/components/modeler/feel/language-guide/feel-unary-tests/). `OutputValue`
exposes implicit conversions for better UX. Both are specialized views on `Expr`.

<!-- @formatter:off -->
```scala
trait UnaryTest[-T] extends Expr[T => Boolean]

opaque type OutputValue[T] <: Expr[T] = Expr[T]
object OutputValue {
  implicit def toLiteral[T](t: T)(using LiteralShow[T]): OutputValue[T] = Literal(t)
  implicit def fromExpr[T](expr: Expr[T]): OutputValue[T] = expr
}
```
<!-- @formatter:on -->

So what is `Expr`? It's an expression that can be statically rendered into a string.
<!-- @formatter:off -->
```scala 
trait Expr[+Out] {
  def evaluate: Out
  def renderExpression: String
}
```
<!-- @formatter:on -->

All the most common ways of building `UnaryTest`s are accessible through `it` object.
Implicit conversion between `Expr[Boolean]` and `UnaryTest` is also defined.

## Quoted Expressions

The easiest way to create expressions is by using Scala quoted code blocks.
This allows to write regular Scala code that will be converted to `Expr` automatically.

```scala
import decisions4s.Expr.quoted

val expr: Expr[Int] = quoted(1 + 1)
```

The code within `quoted` block is analyzed to extract its source code representation.
If the source code cannot be obtained (e.g. when dynamically generating code), a compile-time error will be reported.

## Built-in Expressions

`Decisions4s` provides basic numeric and boolean expressions that can be used by invoking methods on
expressions.
Raw values can be converted into expression through `Literal` and `asLiteral` extension method.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_expr end=end_expr
```

## Custom Expressions

To define a custom expression its enough to extend `Expr` trait.

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_custom_generic end=end_custom_generic
```

This can be further streamlined by using `Function` helper

```scala file=./main/scala/decisions4s/example/docs/ExpressionsExample.scala start=start_custom_streamlined end=end_custom_streamlined
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
