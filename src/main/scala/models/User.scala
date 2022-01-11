package scommender
package models

import org.apache.spark.sql.Row
import scala.util.Try


case class User(
                 userId: Int,
                 cityId: Option[Int],
                 gender: String
               )

object User {
  val dfColNames = Seq("user_id", "city", "gender")

  def fromRow(row: Row): User = User(
    userId = row.getAs[Long]("user_id").toInt,
    cityId = Try(row.getAs[Int]("city")).toOption,
    gender = row.getAs[String]("gender")
  )
}
