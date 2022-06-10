package com.flink.app.aggregator


import com.flink.app.profile.ProfileStreamPipeline.{ProfileKey, ProfileValue}
import com.flink.app.profile.ProfileType
import org.apache.flink.api.common.functions.{AggregateFunction}


class UserProfileAggregator extends AggregateFunction[(ProfileKey,ProfileValue),(ProfileKey,Seq[ProfileValue]),
  (ProfileKey, Seq[ProfileValue])] {
  override def createAccumulator(): (ProfileKey, Seq[ProfileValue]) = {
    (ProfileKey("","",ProfileType.payer.toString), Seq[ProfileValue]())
  }

  override def add(value: (ProfileKey, ProfileValue), accumulator: (ProfileKey, Seq[ProfileValue])): (ProfileKey, Seq[ProfileValue]) = {
    (value._1,accumulator._2 :+ value._2)
  }

  override def getResult(accumulator: (ProfileKey, Seq[ProfileValue])): (ProfileKey, Seq[ProfileValue]) = {
    (accumulator._1,accumulator._2)
  }

  override def merge(a: (ProfileKey, Seq[ProfileValue]), b: (ProfileKey, Seq[ProfileValue])): (ProfileKey, Seq[ProfileValue]) = {
    (a._1,a._2 ++ b._2)
  }
}
