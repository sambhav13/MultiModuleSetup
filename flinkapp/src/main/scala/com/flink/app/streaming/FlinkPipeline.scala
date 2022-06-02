package com.flink.app.streaming

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object FlinkPipeline {

  def main(args:Array[String]) = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    env.setParallelism(1)

    val ds = env.fromElements(1,3,4,5)
    ds.print()

    env.execute()
    ()
  }
}
