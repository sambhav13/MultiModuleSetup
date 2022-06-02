package com.spark.app.streaming

import org.apache.spark.sql.SparkSession

object HudiWriter {

  def main(args:Array[String]) = {

   val spark = SparkSession.builder()
     .master("local[*]")
     .appName("SparkHudiWriter")
     .getOrCreate()

    val ds = spark.range(1,10)

    ds.show(false)

    spark.stop()
  }
}
