name := "sparkapp"

version := "1.0"

scalaVersion := "2.12.7"


val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core" % "3.1.1",
  "org.apache.spark" %% "spark-sql" % "3.1.1"

)

val scalaTestDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)

libraryDependencies ++= sparkDependencies
libraryDependencies ++= scalaTestDependencies
