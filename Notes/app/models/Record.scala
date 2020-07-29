package models

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms._
import reactivemongo.api.bson.BSONDocument

case class Record(
                   _id: Option[UUID],
                   title: String,
                   text: Option[String],
                   complete: Boolean
                 ) {

  def toBSONDocument: BSONDocument = BSONDocument(
    f"$$set" -> BSONDocument(
      "title" -> title,
      "text" -> text,
      "complete" -> complete
    )
  )
}

object Record {
  import play.api.libs.json._

  implicit val recordFormat: OFormat[Record] = Json.format[Record]

//  implicit val form: Form[Record] = Form(
//    mapping(
//      "title" -> nonEmptyText,
//      "text" -> text,
//      "complete" -> boolean
//    )(Record.apply)(Record.unapply)
//  )
}