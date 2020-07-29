package models.dtos

import java.util.UUID

import models.{Record, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping

case class RecordDTO(title: String,
                     text: String,
                     complete: Boolean) {
  def toRecord(userId: Long): Record = {
    Record(
      UUID.randomUUID(),
      userId,
      title,
      Some(text),
      complete
    )
  }
}

object RecordDTO {

  implicit val form: Form[RecordDTO] = Form(
    mapping(
      "title" -> nonEmptyText,
      "text" -> text,
      "complete" -> boolean
    )(RecordDTO.apply)(RecordDTO.unapply)
  )
}
