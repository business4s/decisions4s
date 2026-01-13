import org.typelevel.scalacoptions.ScalacOptions

lazy val `decisions4s` = (project in file("."))
  .settings(commonSettings)
  .aggregate(
    `decisions4s-core`,
    `decisions4s-dmn`,
    `decisions4s-cats-effect`,
    `decisions4s-examples`,
    `decisions4s-dmn-to-image`,
    `decisions4s-sql`,
  )

lazy val `decisions4s-core` = (project in file("decisions4s-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"    %% "shapeless3-deriving" % "3.5.0",
      "com.lihaoyi"      %% "sourcecode"          % "0.4.4",
      ("org.camunda.feel" % "feel-engine"         % "1.20.0" % Test).exclude("com.lihaoyi", "sourcecode_2.13"),
      "ch.qos.logback"    % "logback-classic"     % "1.5.23" % Test,
    ),
  )

lazy val `decisions4s-dmn` = (project in file("decisions4s-dmn"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.camunda.bpm.model" % "camunda-dmn-model" % "7.24.0",
    ),
  )

lazy val `decisions4s-cats-effect` = (project in file("decisions4s-cats-effect"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.6.3",
    ),
  )

lazy val `decisions4s-sql` = (project in file("decisions4s-sql"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC6",
    ),
  )

lazy val `decisions4s-dmn-to-image` = (project in file("decisions4s-dmn-to-image"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.seleniumhq.selenium" % "selenium-java"    % "4.39.0",
      "io.github.bonigarcia"    % "webdrivermanager" % "6.3.3",
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.23" % Test,
    ),
  )

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .settings(
    publish / skip := true,
  )
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`, `decisions4s-cats-effect`, `decisions4s-dmn-to-image`)

lazy val `decisions4s-examples-scala2` = (project in file("decisions4s-examples-scala2"))
  .settings(
    scalaVersion   := "2.13.16",
    libraryDependencies ++= testDeps,
    publish / skip := true,
    scalacOptions ++= Seq("-Ytasty-reader"),
  )
  .dependsOn(`decisions4s-core`)

lazy val commonSettings = Seq(
  scalaVersion  := "3.7.3",
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
  "org.scalatest" %% "scalatest-freespec"       % "3.2.19" % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.19" % Test,
)

lazy val stableVersion = taskKey[String]("stableVersion")

stableVersion := {
  if (isVersionStable.value && !isSnapshot.value) version.value
  else previousStableVersion.value.getOrElse("unreleased")
}

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
