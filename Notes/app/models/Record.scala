package models

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json, OFormat}
import reactivemongo.api.bson.BSONDocument

case class Record(
                   _id: UUID,
                   ownerId: Long,
                   title: String,
                   text: Option[String],
                   complete: Boolean
                 ) {

  def updateFrom(that: Record): Record = copy(
    title = that.title,
    text = that.text,
    complete = that.complete
  )

  def toJson: JsValue = Json.toJson(this)

  def toBSONDocument: BSONDocument = BSONDocument(
    f"$$set" -> BSONDocument(
      "title" -> title,
      "text" -> text,
      "complete" -> complete
    )
  )
}

object Record {

  implicit val format: OFormat[Record] = Json.format[Record]

}