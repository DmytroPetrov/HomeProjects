package services.impl

import com.google.inject.Inject
import com.github.t3hnar.bcrypt._
import exceptions.Exceptions.{TokenBrokenOrExpired, UserAlreadyExist, UserIsNotActive, WrongCredentials}
import models.User
import models.dtos.{CreateUserDTO, Credentials}
import monix.eval.Task
import play.api.Logging
import services.{AuthService, EmailTokenService, MailerService, UserService}
import services.helpers.TimeHelper

class AuthServiceImpl @Inject()(userService: UserService,
                                mailerService: MailerService,
                                emailTokenService: EmailTokenService,
                                timeHelper: TimeHelper) extends AuthService with Logging {

  def confirm(token: String): Task[Unit] = {
    emailTokenService.getByToken(token).flatMap {
      case None => Task.raiseError(TokenBrokenOrExpired)
      case Some(emailToken) if emailTokenService.isExpired(emailToken) =>
        emailTokenService.delete(emailToken).flatMap {_ =>
          Task.raiseError(TokenBrokenOrExpired)
        }
      case Some(emailToken) if !emailTokenService.isExpired(emailToken) =>
        emailTokenService.delete(emailToken).flatMap {_ =>
          userService.activateUser(emailToken.userId)
        }
    }
  }

  def signUp(dto: CreateUserDTO): Task[Unit] = {
    (for {
      userOption <- userService.getByEmail(dto.email)
      tokenOption <- userOption match {
        case Some(user) => emailTokenService.getByUserId(user.id)
        case None => Task.now(None)
      }
    } yield {
      (userOption, tokenOption) match {
        case (Some(user), None) if user.active => Task.raiseError(UserAlreadyExist())
        case (Some(user), None) if !user.active =>
          logger.warn("The user is inactive and the token is expired or broken, sent new token.")
          createAndSendToken(user)
        case (Some(user), Some(token)) if emailTokenService.isExpired(token) =>
          emailTokenService.delete(token).flatMap { _ =>
            createAndSendToken(user)
          }
        case (Some(user), Some(token)) if !emailTokenService.isExpired(token) =>
          mailerService.sendMail(user.email, token.token)
        case (None, _) =>
          userService.create(dto).flatMap { user =>
            createAndSendToken(user)
          }
      }
    }).flatten
  }

  private def createAndSendToken(user: User): Task[Unit] = {
    emailTokenService.create(user.id).flatMap { x =>
      mailerService.sendMail(user.email, x.token)
    }
  }

  def signIn(credentials: Credentials): Task[Long] = {
    userService.getByEmail(credentials.email) flatMap  {
      case Some(user) if !credentials.password.isBcrypted(user.password) => Task.raiseError(WrongCredentials)
      case Some(user) if !user.active => Task.raiseError(UserIsNotActive)
      case Some(user) => Task.now(user.id)
      case _ => Task.raiseError(WrongCredentials)
    }
  }

}
