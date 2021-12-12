package ir.ac.usc
package utils

import org.apache.spark.sql.types.{DataTypes, StructField, StructType}

object DataFrameSchemas {

  val usersSchema: StructType = StructType(Seq(
    StructField(name = "user_id", dataType = DataTypes.LongType, nullable = false),
    StructField(name = "city", dataType = DataTypes.IntegerType, nullable = true),
    StructField(name = "gender", dataType = DataTypes.StringType, nullable = true)
  )
  )

  val songsStruct: StructType = StructType(Seq(
    StructField(name = "song_id", dataType = DataTypes.LongType, nullable = false),
    StructField(name = "name", dataType = DataTypes.StringType, nullable = true),
    StructField(name = "length", dataType = DataTypes.LongType, nullable = true),
    StructField(name = "genre_ids", dataType = DataTypes.StringType, nullable = true), // separated by |
    StructField(name = "artist_name", dataType = DataTypes.StringType, nullable = true),
    StructField(name = "language", dataType = DataTypes.DoubleType, nullable = true)
  ))

  val ratingsStruct: StructType = StructType(
    Seq(
      StructField(name = "user_id", dataType = DataTypes.LongType),
      StructField(name = "song_id", dataType = DataTypes.LongType),
      StructField(name = "target", dataType = DataTypes.DoubleType)
    )
  )

  val testStruct: StructType = StructType(
    Seq(
      StructField(name = "user_id", dataType = DataTypes.LongType),
      StructField(name = "song_id", dataType = DataTypes.LongType)
    )
  )

}
