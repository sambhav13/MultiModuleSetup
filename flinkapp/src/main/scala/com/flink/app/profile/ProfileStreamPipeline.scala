package com.flink.app.profile

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.flink.app.aggregator.UserProfileAggregator

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger

import java.time.LocalTime
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.producer.ProducerRecord

import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.util.Properties

object ProfileStreamPipeline extends Serializable{

  case class ProfileKey(accountNumber:String, IFSC_Code: String, profileType: String)
  case class ProfileValue(settlementAmount:Double, txnId:String,
                          @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Kolkata")
                          txnInitTimestamp:Timestamp)
  case class Profile(key:ProfileKey,value:Seq[ProfileValue])
  case class Transaction(payee_ifsc_code:String,
                         payee_account_number:String,
                         payee_settlement_amount:Double,
                         payer_ifsc_code:String,
                         payer_account_number:String,
                         payer_settlement_amount :Double,
                         txnId:String,
                         @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Kolkata")
                         txnInitTimestamp:Timestamp)

  def main(args:Array[String]) = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    env.setParallelism(1)

    val source: KafkaSource[String] = KafkaSource.builder()
      .setBootstrapServers("localhost:9092")
      .setTopics("gw7")
      .setGroupId("gw-group-"+LocalTime.now().getSecond)
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build();
    val stream: DataStream[String] =  env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

   // stream.print()
   // val stream = env.fromElements("user1,1","user2,4","user1,3","user3,6","user1,9","user4,11","user2,5")
   // val stream = env.fromElements("{\n  \"payee_ifsc_code\": \"SBIN0005943\",\n  \"payee_account_number\" : \"10021412000123\",\n  \"payee_settlement_amount\" : 106.9,\n  \"payer_ifsc_code\":\"HDFC0006943\",\n  \"payer_account_number\":\"1212312312312\",\n  \"payer_settlement_amount\": 106.9,\n  \"txnId\":\"PTM97de7823239sdfs923e23\" ,\n \"txnInitTimestamp\": \"2022-05-31 11:28:52.322\" \n }",
   // "{\n  \"payee_ifsc_code\": \"SBIN0005944\",\n  \"payee_account_number\" : \"10021412000124\",\n  \"payee_settlement_amount\" : 107.9,\n  \"payer_ifsc_code\":\"HDFC0006943\",\n  \"payer_account_number\":\"1212312312312\",\n  \"payer_settlement_amount\": 107.9,\n  \"txnId\":\"PTM97de7823239sdfs923e24\",\n  \"txnInitTimestamp\":\"2022-06-01 11:28:52.322\"\n}")

    val userStream: DataStream[Transaction] =
      stream.map( p =>  {
        val objectMapper = new ObjectMapper()
        objectMapper.registerModule(DefaultScalaModule)
        val obj = objectMapper.readValue(p,classOf[Transaction])
        obj
      })

    val userProfileStream: DataStream[(ProfileKey, ProfileValue)] = userStream
      .map(p => {
        val pk = ProfileKey(p.payer_account_number,p.payer_ifsc_code,ProfileType.payer.toString)
        val pv = ProfileValue(p.payer_settlement_amount,p.txnId,p.txnInitTimestamp)
        (pk,pv)
      })

    val userProfileKeyedStream: DataStream[(ProfileKey, Seq[ProfileValue])] = userProfileStream
          .keyBy(_._1)
          .window(GlobalWindows.create())
          .trigger(CountTrigger.of(2))
          .aggregate(new UserProfileAggregator())

    val profiledStream = userProfileKeyedStream.map(p => {
      Profile(p._1,p._2)
    })

    profiledStream.print()
    val properties = new Properties
    properties.setProperty("bootstrap.servers", "localhost:9092")

    val serializationSchema = new KafkaSerializationSchema[Profile] {
      override def serialize(element: Profile,
                             timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {

        val objectMapper = new ObjectMapper()
        objectMapper.registerModule(DefaultScalaModule)
        val  data: String = {
          try {
            val str = objectMapper.writeValueAsString(element)
            str
          }
          catch {
            case e: JsonProcessingException => {
              println("Failed to parse JSON", e)
              ""
            }
          }

        }

        val p = new ProducerRecord[Array[Byte], Array[Byte]](
          "profilesink-try15",      // target topic
          data.getBytes(StandardCharsets.UTF_8)) // record contents
        p
      }
    }

    val myProducer = new FlinkKafkaProducer[Profile](
      "profilesink-try15",                  // target topic
      serializationSchema,         // serialization schema
      properties,                  // producer config
      FlinkKafkaProducer.Semantic.EXACTLY_ONCE)
    profiledStream.addSink(myProducer)
    env.execute()
    ()
  }
}

object ProfileType extends Enumeration
{
  type ProfileType = Value
  // Assigning Values
  val payer = Value(0, "Payer")
  val payee = Value(1, "Payee")

}
