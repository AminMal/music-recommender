package ir.ac.usc
package models

import org.apache.spark.sql.Row

import scala.util.Try

case class Song(
                 id: Long,
                 name: String,
                 artistName: Option[String]
               )

object Song {
  def fromRow(r: Row): Song = Song(
    id = r.getString(0).toLong,
    name = r.getString(1),
    artistName = Try(r.getString(2)).toOption
  )
}
