package testUtils

import models.{EmailToken, Record, User}
import models.dtos.{CreateUserDTO, Credentials, RecordDTO}
import services.helpers.TimeHelper

trait TestDataUtils {

  implicit val th: TimeHelper

  val correctUserDTO: CreateUserDTO = CreateUserDTO(
    "Lorem",
    "ipsum",
    "dolor@sit.amet",
    "consectetur"
  )

  lazy val inactiveUser: User = correctUserDTO.toUser()

  lazy val activeUser: User = inactiveUser.copy(active = true)

  lazy val foreignUser: User = activeUser.copy(id = activeUser.id + 1)

  lazy val emailToken: EmailToken = EmailToken(inactiveUser.id, "token", th.now())

  val correctCredentials: Credentials = Credentials("dolor@sit.amet", "consectetur")

  val incorrectCredentials: Credentials = Credentials("WRONG@sit.amet", "WRONG")


}
