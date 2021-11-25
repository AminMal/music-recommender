package ir.ac.usc
package models

import org.apache.spark.sql.Row

import scala.util.Try

case class User(
               userId: Int,
               cityId: Option[Int],
               gender: String
               ) {
  def decoupled() = (userId.toLong, cityId.getOrElse(0), gender)
}

object User {
  val dfColNames = Seq("user_id", "city", "gender")
  def fromRow(row: Row): User = User(
    userId = row.getLong(0).toInt,
    cityId = Try(row.getInt(1)).toOption,
    gender = row.getString(2)
  )
}
