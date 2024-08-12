---
sidebar_position: 2.1
---

# Scala 2 Support

Currently, `Decisions4s` is published only for Scala 3. Publishing it for Scala 2 is not feasible due to its reliance on
the latest Scala features.

We believe the Scala ecosystem should migrate to Scala 3 as soon as possible. However, we understand that business
constraints may require maintaining Scala 2 codebases for the foreseeable future.

**Using `Decisions4s` from Scala 2 is possible but comes with challenges.**

We recommend the following approach:

* Create a separate module, `your-app-decisions`, that uses Scala 3 and depends only on `Decisions4s`. Define your
  decisions within this module.
    * This setup is necessary to leverage typeclass derivation based on macros, which can only be invoked from a Scala 3
      module. Additionally, rule definitions rely on context functions, which are not available in Scala 2.
* Take advantage of
  the [compatibility between Scala 2 and Scala 3](https://docs.scala-lang.org/scala3/guides/migration/compatibility-classpath.html#a-scala-213-module-can-depend-on-a-scala-3-artifact)
  by using `.dependsOn()` to link the new Scala 3 module to your main Scala 2 module. This allows you to evaluate and
  visualize decisions from within your Scala 2 codebase.
* If you're using `decisions4s-cats-effect`, exclude the `cats-effect` dependency to avoid conflicts where both Scala 3
  and Scala 2 versions end up on the classpath. Although this isn't entirely safe in theory, it should work in practice.
  Ensure that you thoroughly test your decision-evaluation code to confirm compatibility.

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