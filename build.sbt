ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.7"

lazy val root = (project in file("."))
  .settings(
    name := "ProfileCreationMultiProjectSetup"
  )
  .aggregate(flinkapp, sparkapp)

lazy val commonDependencies = Seq(
   "org.scalactic" %% "scalactic" % "3.2.12",
   "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)

lazy val flinkapp = (project in file("flinkapp"))
  .settings(
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )

lazy val sparkapp = (project in file("sparkapp"))
  .settings(
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(flinkapp)


lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
  }
)

