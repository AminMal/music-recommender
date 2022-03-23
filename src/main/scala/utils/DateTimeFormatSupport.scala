package scommender
package utils

import spray.json.{JsString, JsValue, RootJsonFormat}

import java.time.LocalDateTime

trait DateTimeFormatSupport {

  implicit val localDateTimeFormatter: RootJsonFormat[LocalDateTime] = new RootJsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = JsString(obj.toString)

    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(value) => LocalDateTime.parse(value)
      case other => throw new UnsupportedOperationException(s"cannot cast $other as LocalDateTime")
    }
  }

}
