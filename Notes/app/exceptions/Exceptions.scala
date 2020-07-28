package exceptions

import play.api.data.FormError

object Exceptions {

  case class WrongJsonException(errors: Seq[FormError]) extends RuntimeException(s"Wrong json.\n$errors")

  case class NoFilePassed(key: String) extends RuntimeException(s"No file with key=$key passed to multipart form data")


  case object NoJsonException extends RuntimeException("No json passed to request")

  case object NoStringException extends RuntimeException("No string passed to request")

  case object NoMultipartFormData extends RuntimeException("No multipart form data passed")

  case class WrongParameterException(message: String) extends RuntimeException(s"Wrong parameter exception.\n$message")



  case object WrongCredentials extends RuntimeException("Wrong credentials")

  case object UnauthorizedException extends RuntimeException("Unauthorized")

  case class ForbiddenException(message: String) extends RuntimeException("Forbidden. " + message)

  case class NotFoundException(message: String) extends RuntimeException(message)
  object NotFoundException {
    def apply(model: String, cond: String): NotFoundException = new NotFoundException(s"$model with $cond not found!")
  }

  case class InternalException(message: String = "Internal exception") extends RuntimeException(message)

  case class DbException(e: Throwable) extends RuntimeException("DB exception", e)

  case object DbResultException extends RuntimeException("No raws was affected")

  case class UserAlreadyExist(message: String = "User already exist") extends RuntimeException(message)

  case object TokenBrokenOrExpired extends RuntimeException


  case object UserIsNotActive extends RuntimeException("User has't confirmed his email or was deactivated")

}
