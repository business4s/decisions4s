lazy val `decisions4s` = (project in file("."))
  .settings(commonSettings)
  .aggregate(`decisions4s-core`, `decisions4s-examples`)

lazy val `decisions4s-core` = (project in file("decisions4s-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "shapeless3-deriving" % "3.4.0",
      "org.scalameta"   %% "munit"               % "1.0.0"  % Test,
      "org.camunda.feel" % "feel-engine"         % "1.17.7" % Test,
      "ch.qos.logback"   % "logback-classic"     % "1.5.6"  % Test,
    ),
  )

lazy val `decisions4s-dmn` = (project in file("decisions4s-dmn"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.camunda.bpm.model" % "camunda-dmn-model" % "7.21.0",
    ),
  )

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`)

lazy val commonSettings = Seq(
  scalaVersion := "3.3.3",
  scalacOptions ++= Seq("-no-indent", "-Yrangepos"),
  libraryDependencies ++= testDeps,
)

lazy val testDeps = List(
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
)
