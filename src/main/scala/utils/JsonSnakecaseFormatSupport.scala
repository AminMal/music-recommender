package ir.ac.usc
package utils

import spray.json.DefaultJsonProtocol


trait JsonSnakecaseFormatSupport extends DefaultJsonProtocol {
  import reflect._

  override protected def extractFieldNames(classTag: ClassTag[_]): Array[String] = {
    import java.util.Locale

    def snakecase(name: String): String = PASS2.replaceAllIn(PASS1.replaceAllIn(name, REPLACEMENT), REPLACEMENT)
      .toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map(snakecase)
  }

  private val PASS1 = """([A-Z]+)([A-Z][a-z])""".r
  private val PASS2 = """([a-z\d])([A-Z])""".r
  private val REPLACEMENT = "$1_$2"
}

object JsonSnakecaseFormatSupport extends JsonSnakecaseFormatSupport