---
sidebar_position: 1.1
---

# Scala 2 Support

As of now, `Decisions4s` is published only for Scala 3. Publishing it for Scala 2, while theoretically possible, would
add a significant burden on the library by limiting the language features that can be used and/or requiring significant
code duplication.

We believe that the Scala ecosystem should migrate to Scala 3 as soon as possible. Having said that, we also understand
the business constraints and hence the need to run Scala 2 codebases for the foreseeable future.

**Using `Decisions4s` from Scala 2 is possible but inconvenient.**

We recommend the following approach:

* Define a separate module `your-app-decisions` that will use Scala 3 and depend only on `Decisions4s`. Define your
  decisions there.
  This is needed to leverage typeclass derivation that is based on macros that can be invoked only from Scala 3 module.

  Alternatively the decisions can be defined directly in Scala 2 module, but this will require writing `HKD` instance by
  hand or implementing a custom derivation mechanism.
* Leverage
  the [ability to use Scala 3 from Scala 2](https://docs.scala-lang.org/scala3/guides/migration/compatibility-classpath.html#a-scala-213-module-can-depend-on-a-scala-3-artifact)
  and `.dependsOn()` on the new module from the main one.
  You can use all the features of Decisions4s (other than typeclass derivation) from within Scala 2 codebase.
* If you're using `decisions4s-cats-effect`, exclude the cats-effect dependency,
  to avoid the situation where both scala-3 and scala-2 versions land on the classpath. This is not entirely safe in
  theory but should work in practice.
  Testing your decision-evaluation code should be enough to check this.

## Example setup

```scala

val myServiceDecisions = project
  .in(file("my-service-decisions"))
  .settings(
    scalaVersion := "3.3.3",
    libraryDependencies ++= Seq(
      "org.business4s" %% "decisions4s-core" % "xxx",
      ("some.other" %% "scala-lib-published-for-scala-2" % "yyy").cross(CrossVersion.for3Use2_13),
    ),
  )

val myService = project
  .in(file("my-service"))
  .settings(
    scalaVersion := "2.13.14",
    libraryDependencies ++= Seq(
      ("org.business4s" %% "decisions4s-cats-effect" % "xxx").cross(CrossVersion.for2_13Use3).excludeAll("org.typelevel"),
      ("org.business4s" %% "decisions4s-dmn" % "xxx").cross(CrossVersion.for2_13Use3),
    ),
  )
  .depenedsOn(myServiceDecisions)
```