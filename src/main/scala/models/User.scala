package ir.ac.usc
package models

import org.apache.spark.sql.Row

import scala.util.Try

case class User(
               userId: Int,
               cityId: Option[Int],
               gender: String
               )

object User {
  def fromRow(row: Row): User = User(
    userId = row.getString(0).toInt,
    cityId = Try(row.getString(1).toInt).toOption,
    gender = row.getString(2)
  )
}
