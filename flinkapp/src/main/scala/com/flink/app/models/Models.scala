package com.flink.app.models

object Models {

  case class Transaction(id: String, description: String, userId:String)
  case class User(id:String)
  case class UserSum(user:String, count:Int)
}
