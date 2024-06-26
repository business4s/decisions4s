lazy val `decisions4s` = (project in file("."))
  .settings(commonSettings)
  .aggregate(
    `decisions4s-core`,
    `decisions4s-dmn`,
    `decisions4s-cats-effect`,
    `decisions4s-examples`,
    `decisions4s-dmn-to-image`,
  )

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

lazy val `decisions4s-cats-effect` = (project in file("decisions4s-cats-effect"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.scalameta" %% "munit"       % "1.0.0" % Test,
    ),
  )

lazy val `decisions4s-dmn-to-image` = (project in file("decisions4s-dmn-to-image"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.seleniumhq.selenium" % "selenium-java"    % "4.21.0",
      "io.github.bonigarcia"    % "webdrivermanager" % "5.8.0",
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.6" % Test,
    ),
  )

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .settings(
    publish / skip := true,
  )
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`, `decisions4s-cats-effect`, `decisions4s-dmn-to-image`)

lazy val commonSettings = Seq(
  scalaVersion  := "3.3.3",
  scalacOptions ++= Seq("-no-indent"),
  libraryDependencies ++= testDeps,
  organization  := "org.business4s",
  homepage      := Some(url("https://business4s.github.io/decisions4s/")),
  licenses      := List(License.MIT),
  developers    := List(
    Developer(
      "Krever",
      "Voytek Pituła",
      "w.pitula@gmail.com",
      url("https://v.pitula.me"),
    ),
  ),
  versionScheme := Some("semver-spec"),
)

lazy val testDeps = List(
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
)

lazy val stableVersion = taskKey[String]("stableVersion")

stableVersion := {
  if (isVersionStable.value && !isSnapshot.value) version.value
  else previousStableVersion.value.getOrElse("unreleased")
}
