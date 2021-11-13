package ir.ac.usc

import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Logger, Level}

object Bootstrap {

  Logger.getLogger("org").setLevel(Level.ERROR)

  lazy final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local")  // todo, this needs to be removed in production
    .getOrCreate()

}
