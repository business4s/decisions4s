---
sidebar_position: 1.1
---

# Scala 2 Support

As of now, `Decisions4s` is published only for Scala 3. Publishing it for Scala 2, while theoretically possible, would
add a significant burden on the library by limiting the language features that can be used, and/or requiring significant
code duplication.

We also believe the Scala ecosystem should migrate to Scala 3 as soon as possible. Having said that, we also understand
the business constraints and hence the need to run Scala 2 codebases for the foreseeable future.

**To use `Decisions4s` from Scala 2 we recommend the following approach**:

* Define a separate module `your-app-decisions` that will use Scala 3 and depend only on `Decisions4s`. Define your
  decisions there.
* Leverage
  the [ability to use Scala 3 from Scala 2](https://docs.scala-lang.org/scala3/guides/migration/compatibility-classpath.html#a-scala-213-module-can-depend-on-a-scala-3-artifact)
  and `.dependsOn()` on the new module from the main one.

This is required because `decisions4s` comes with typeclass derivation based on shapeless-3,
and the macros involved there have to be expanded in the context of Scala 3 module.

