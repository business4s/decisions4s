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

lazy val `decisions4s-dmn`         = (project in file("decisions4s-dmn"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.camunda.bpm.model" % "camunda-dmn-model" % "7.21.0",
    ),
  )

lazy val `decisions4s-cats-effect` = (project in file("decisions4s-cats-effect"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.scalameta" %% "munit"       % "1.0.0" % Test,
    ),
  )

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`, `decisions4s-cats-effect`)

lazy val commonSettings = Seq(
  scalaVersion := "3.3.3",
  scalacOptions ++= Seq("-no-indent"),
  libraryDependencies ++= testDeps,
  dynverSonatypeSnapshots := true,
  organization := "com.github.krever", // temporary, until new ns is claimed
  homepage := Some(url("https://business4s.github.io/decisions4s/")),
  licenses := List(License.MIT),
  developers := List(
    Developer(
      "Krever",
      "Voytek Pituła",
      "w.pitula@gmail.com",
      url("https://v.pitula.me")
    )
  )
)

lazy val testDeps = List(
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
)

lazy val stableVersion = taskKey[String]("stableVersion")

stableVersion := {
  if(isVersionStable.value && !isSnapshot.value) version.value
  else previousStableVersion.value.getOrElse("unreleased")
}