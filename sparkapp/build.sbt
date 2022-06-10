name := "sparkapp"

version := "1.0"

scalaVersion := "2.12.7"


val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-sql" % "3.1.1" ,
  "org.apache.spark" %% "spark-core" % "3.1.1" exclude("org.json4s","json4s-ast") exclude("org.json4s","json4s-jackson") exclude("org.json4s","json4s-core") exclude("org.json4s","json4s-scalap"),
  "org.apache.hudi" %%  "hudi-spark3.1-bundle" % "0.11.0",
  "org.apache.spark" %% "spark-avro" % "3.1.1",
  "org.apache.hadoop" % "hadoop-aws" % "3.2.0",
  "joda-time" % "joda-time" % "2.9.4",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.1"

)

val scalaTestDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)

libraryDependencies ++= sparkDependencies
libraryDependencies ++= scalaTestDependencies

