package models.dtos

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

case class Credentials(email: String, password: String)

object Credentials{
  implicit val form: Form[Credentials] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
    )(Credentials.apply)(Credentials.unapply)
  )
}
