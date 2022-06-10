name := "flinkapp"

version := "1.0"

scalaVersion := "2.12.7"

val flinkVersion = "1.14.4"


val flinkDependencies = Seq(

  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2",
  "org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-table-planner-blink" % "1.13.6" % "provided",
  "org.apache.flink" %% "flink-connector-kafka" % "1.14.4" ,
  // https://mvnrepository.com/artifact/org.apache.flink/flink-runtime-web
  "org.apache.flink" %% "flink-runtime-web" % "1.14.4",

  //  "org.apache.avro" %% "avro" % "1.8.2",
  "org.apache.flink" %% "flink-table-api-scala" % "1.14.4",
  "org.apache.flink" %% "flink-table-planner" % "1.14.4" % "provided",
  "org.apache.flink" %% "flink-parquet" % "1.14.4",
  "org.apache.avro" % "avro" % "1.11.0",
  "org.apache.hadoop" % "hadoop-common" % "3.0.0",
  "org.apache.hadoop" % "hadoop-core" % "1.2.1",
  "org.apache.parquet" % "parquet-avro" % "1.11.1" exclude("org.apache.hadoop","hadoop-client") exclude("it.unimi.dsi","fastutil"),
  "org.slf4j" % "slf4j-simple" % "1.7.36",
  "org.slf4j" % "slf4j-api" % "1.7.36"
)

libraryDependencies ++= flinkDependencies