import org.typelevel.scalacoptions.ScalacOptions

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
      "org.typelevel"    %% "shapeless3-deriving" % "3.4.0",
      "com.lihaoyi"      %% "sourcecode"          % "0.4.2",
      ("org.camunda.feel" % "feel-engine"         % "1.19.3" % Test).exclude("com.lihaoyi", "sourcecode_2.13"),
      "ch.qos.logback"    % "logback-classic"     % "1.5.18" % Test,
    ),
  )

lazy val `decisions4s-dmn` = (project in file("decisions4s-dmn"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.camunda.bpm.model" % "camunda-dmn-model" % "7.23.0",
    ),
  )

lazy val `decisions4s-cats-effect` = (project in file("decisions4s-cats-effect"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.7",
    ),
  )

lazy val `decisions4s-dmn-to-image` = (project in file("decisions4s-dmn-to-image"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.seleniumhq.selenium" % "selenium-java"    % "4.32.0",
      "io.github.bonigarcia"    % "webdrivermanager" % "6.1.0",
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.18" % Test,
    ),
  )

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .settings(
    publish / skip := true,
  )
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`, `decisions4s-cats-effect`, `decisions4s-dmn-to-image`)

lazy val `decisions4s-examples-scala2` = (project in file("decisions4s-examples-scala-2"))
  .settings(
    scalaVersion   := "2.13.16",
    libraryDependencies ++= testDeps,
    publish / skip := true,
    scalacOptions ++= Seq("-Ytasty-reader"),
  )
  .dependsOn(`decisions4s-core`)

lazy val commonSettings = Seq(
  scalaVersion  := "3.7.0",
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
  Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement,
)

lazy val testDeps = List(
  "org.scalatest" %% "scalatest-freespec" % "3.2.19" % Test,
)

lazy val stableVersion = taskKey[String]("stableVersion")

stableVersion := {
  if (isVersionStable.value && !isSnapshot.value) version.value
  else previousStableVersion.value.getOrElse("unreleased")
}
