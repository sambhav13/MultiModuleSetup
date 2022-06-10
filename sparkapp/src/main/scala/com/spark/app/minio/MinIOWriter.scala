  package com.spark.app.minio

  import com.fasterxml.jackson.core.JsonProcessingException
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import com.spark.app.models.Models.{Profile, ProfileKey, ProfileValue}
  import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
  import org.apache.spark.sql.types.{ArrayType, DoubleType, IntegerType, StringType, StructField, StructType, TimestampType}
  import org.apache.hudi.DataSourceWriteOptions.{COMMIT_METADATA_KEYPREFIX_OPT_KEY, COW_TABLE_TYPE_OPT_VAL, MOR_TABLE_TYPE_OPT_VAL, PARTITIONPATH_FIELD, PRECOMBINE_FIELD, RECORDKEY_FIELD_OPT_KEY, TABLE_NAME_OPT_KEY, TABLE_TYPE, TABLE_TYPE_OPT_KEY}
  import org.apache.spark.sql.functions.col

  object MinIOWriter {
   def main(args:Array[String]) = {
     val spark = SparkSession
       .builder
       .master("local[*]")
       .appName("StructuredNetworkWordCount")
       .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
       .getOrCreate()

     if(args.length !=2) {
       println("arguments less than 2")
       System.exit(-1)
     }


     val bootStrapServers = args(0)
     val checkpointLocation = args(1)

     val s3accessKeyAws = "minio"
     val s3secretKeyAws = "minio123"
     val connectionTimeOut = "600000"
     val s3endPointLoc: String = "http://127.0.0.1:9000"
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", s3endPointLoc)
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", s3accessKeyAws)
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", s3secretKeyAws)
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.timeout", connectionTimeOut)

     spark.sparkContext.hadoopConfiguration.set("spark.sql.debug.maxToStringFields", "100")
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
     spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "true")

     // Set Spark logging level to ERROR to avoid various other logs on console.
     spark.sparkContext.setLogLevel("ERROR")

     // Create DataFrame representing the stream of input lines from connection to localhost:9999
     val df = spark
       .readStream
       .format("kafka")
       .option("kafka.bootstrap.servers", bootStrapServers)
       //.option("subscribe", "profilesink1")
       .option("subscribe", "profilesink-try15")
       .option("startingOffsets", "earliest")
       .load()

     import spark.sqlContext.implicits._

     val dataSchema: StructType = StructType(
       List(
         StructField("user", StringType, true),
         StructField("count", IntegerType, true)
       )
     )

     val ecolumn = (ele:String) => {
       val objectMapper = new ObjectMapper()
       objectMapper.registerModule(DefaultScalaModule)
       val  data: Profile = {
         try {
           val obj: Profile = objectMapper.readValue(ele, classOf[Profile])
           obj
         }
         catch {
           case e: JsonProcessingException => {
             println("Failed to parse JSON", e)
             Profile(ProfileKey("error", "error", "error"), Seq.empty[ProfileValue])
           }
         }
       }
         data
     }


     val extractColumn = spark.udf.register("extractColumn",ecolumn)
     val ds = df.select($"value".cast(StringType))
     val ds_new = ds.withColumn("value",extractColumn(col("value")))

     val dt: Dataset[Profile] =  ds_new.select($"value.key",$"value.value").as[Profile]
     val words = dt
     words.printSchema()

     // words.show()
     val wordDs = words

     val query = wordDs
       .writeStream
       .queryName("hudi2-writer")
       .foreachBatch { (batchDF: Dataset[Profile], _: Long) => {
         // batchDF.persist()
         batchDF.show(false)
         batchDF.printSchema()

         batchDF.write.format("org.apache.hudi")
           .option(TABLE_TYPE_OPT_KEY, "MERGE_ON_READ")
           .option(MOR_TABLE_TYPE_OPT_VAL, "MERGE_ON_READ")
           .option(PRECOMBINE_FIELD.key(), "key.accountNumber")
           .option(COMMIT_METADATA_KEYPREFIX_OPT_KEY, "key.accountNumber")
           .option(RECORDKEY_FIELD_OPT_KEY, "key.accountNumber")
           .option(PARTITIONPATH_FIELD.key(), "key.profileType")
           .option(TABLE_NAME_OPT_KEY, "merge_on_read_table")
           .option("hoodie.table.name", "merge_on_read_table")
           .mode(SaveMode.Append)
           .save("s3a://profiles4/MERGE_ON_READ")
         batchDF.unpersist(true)
         ()

       }
       }
       .option("checkpointLocation", checkpointLocation)
       .start()

     query.awaitTermination()

   }
  }

