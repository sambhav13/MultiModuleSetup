package com.spark.app.models

import com.fasterxml.jackson.annotation.JsonFormat

import java.sql.Timestamp

object Models {
  case class ProfileKey(accountNumber:String, IFSC_Code: String, profileType: String)
  case class ProfileValue(settlementAmount:Double, txnId:String,
                          @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Kolkata")
                          txnInitTimestamp:Timestamp)
  case class Profile(key:ProfileKey,value:Seq[ProfileValue])
}
