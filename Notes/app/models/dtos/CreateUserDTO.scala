package models.dtos

import com.github.t3hnar.bcrypt._
import models.User
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, nonEmptyText}
import services.helpers.TimeHelper

case class CreateUserDTO(
                          firstName: String,
                          lastName: String,
                          email: String,
                          password: String
                        ) {

  def toUser()(implicit th: TimeHelper): User = {
    val now = th.now()
    User(
      -1,
      this.firstName,
      this.lastName,
      this.email,
      this.password.bcrypt(CreateUserDTO.BCryptRounds),
      active = false,
      now,
      now
    )
  }

}

object CreateUserDTO {

  implicit val form: Form[CreateUserDTO] = Form(
    mapping(
      "firstName" -> nonEmptyText(maxLength = 100),
      "lastName" -> nonEmptyText(maxLength = 100),
      "email" -> Forms.email,
      "password" -> nonEmptyText(8, 64)
    )(CreateUserDTO.apply)(CreateUserDTO.unapply)
  )

  val BCryptRounds: Int = 14

}