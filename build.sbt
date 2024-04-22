lazy val `decisions4s`      = (project in file("."))
  .settings(commonSettings)
  .aggregate(`decisions4s-core`, `decisions4s-examples`)

lazy val `decisions4s-core` = (project in file("decisions4s-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-tagless-core" % "0.16.0"
    )
  )

lazy val `decisions4s-dmn` = (project in file("decisions4s-dmn"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`)

lazy val `decisions4s-examples` = (project in file("decisions4s-examples"))
  .settings(commonSettings)
  .dependsOn(`decisions4s-core`, `decisions4s-dmn`)

lazy val commonSettings = Seq(
  scalaVersion := "3.3.3",
  scalacOptions ++= Seq("-no-indent"),
  libraryDependencies ++= testDeps
)

lazy val testDeps = List(
  "org.scalatest"        %% "scalatest"          % "3.2.17" % Test,
)