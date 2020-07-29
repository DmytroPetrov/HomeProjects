package services.impl

import java.time.Instant

import exceptions.Exceptions.{TokenBrokenOrExpired, UserAlreadyExist, UserIsNotActive, WrongCredentials}
import models.dtos.CreateUserDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import services.{EmailTokenService, MailerService, UserService}
import services.helpers.TimeHelper
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class AuthServiceImplTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {
  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val userServiceMock = mock[UserService]
  private val emailTokenServiceMock = mock[EmailTokenService]
  private val mailerServiceMock = mock[MailerService]
  private val service = new AuthServiceImpl(userServiceMock, mailerServiceMock, emailTokenServiceMock, th)

  "confirm" should {

    "delete token and activate user" in {
      (emailTokenServiceMock.getByToken _).expects(emailToken.token).returns(Task.now(Some(emailToken)))
      (emailTokenServiceMock.isExpired _).expects(emailToken).returns(false).twice
      (emailTokenServiceMock.delete _).expects(emailToken).returns(Task.now(()))
      (userServiceMock.activateUser(_: Long)(_: TimeHelper)).expects(emailToken.userId, TimeHelper.realTimeHelper).returns(Task.now(()))
      service.confirm(emailToken.token).get
    }

    "delete token and return Task with TokenBrokenOrExpired exception" in {
      (emailTokenServiceMock.getByToken _).expects(emailToken.token).returns(Task.now(Some(emailToken)))
      (emailTokenServiceMock.isExpired _).expects(emailToken).returns(true)
      (emailTokenServiceMock.delete _).expects(emailToken).returns(Task.now(()))
      a[TokenBrokenOrExpired.type] should be thrownBy service.confirm(emailToken.token).get
    }

    "return Task with TokenBrokenOrExpired exception" in {
      (emailTokenServiceMock.getByToken _).expects(emailToken.token).returns(Task.now(None))
      a[TokenBrokenOrExpired.type] should be thrownBy service.confirm(emailToken.token).get
    }

  }

  "signUp" should {

    "return Task with UserAlreadyExist exception" in {
      (userServiceMock.getByEmail _).expects(correctUserDTO.email).returns(Task.now(Some(activeUser)))
      (emailTokenServiceMock.getByUserId _).expects(activeUser.id).returns(Task.now(None))
      a[UserAlreadyExist] should be thrownBy service.signUp(correctUserDTO).get
    }

    "create and send new token (note: this case should never happen in prod)" in {
      (userServiceMock.getByEmail _).expects(correctUserDTO.email).returns(Task.now(Some(inactiveUser)))
      (emailTokenServiceMock.getByUserId _).expects(inactiveUser.id).returns(Task.now(None))
      (emailTokenServiceMock.create _).expects(inactiveUser.id).returns(Task.now(emailToken))
      (mailerServiceMock.sendMail _).expects(inactiveUser.email, emailToken.token).returns(Task.now(()))
      service.signUp(correctUserDTO).get
    }

    "delete expired token, create and send new token" in {
      (userServiceMock.getByEmail _).expects(correctUserDTO.email).returns(Task.now(Some(inactiveUser)))
      (emailTokenServiceMock.getByUserId _).expects(inactiveUser.id).returns(Task.now(Some(emailToken)))
      (emailTokenServiceMock.isExpired _).expects(emailToken).returns(true)
      (emailTokenServiceMock.delete _).expects(emailToken).returns(Task.now(()))
      (emailTokenServiceMock.create _).expects(inactiveUser.id).returns(Task.now(emailToken))
      (mailerServiceMock.sendMail _).expects(inactiveUser.email, emailToken.token).returns(Task.now(()))
      service.signUp(correctUserDTO).get
    }

    "resend existing token" in {
      (userServiceMock.getByEmail _).expects(correctUserDTO.email).returns(Task.now(Some(inactiveUser)))
      (emailTokenServiceMock.getByUserId _).expects(inactiveUser.id).returns(Task.now(Some(emailToken)))
      (emailTokenServiceMock.isExpired _).expects(emailToken).returns(false).twice
      (mailerServiceMock.sendMail _).expects(inactiveUser.email, emailToken.token).returns(Task.now(()))
      service.signUp(correctUserDTO).get
    }

    "create user, create and send token" in {
      (userServiceMock.getByEmail _).expects(correctUserDTO.email).returns(Task.now(None))
      (userServiceMock.create(_: CreateUserDTO)(_: TimeHelper)).expects(correctUserDTO, TimeHelper.realTimeHelper).returns(Task.now(inactiveUser))
      (emailTokenServiceMock.create _).expects(inactiveUser.id).returns(Task.now(emailToken))
      (mailerServiceMock.sendMail _).expects(inactiveUser.email, emailToken.token).returns(Task.now(()))
      service.signUp(correctUserDTO).get
    }

  }

  "signIn" should {
    "raise WrongCredentials exception because of wrong email" in {
      (userServiceMock.getByEmail _).expects(incorrectCredentials.email).returns(Task.now(None))
      a[WrongCredentials.type] should be thrownBy service.signIn(incorrectCredentials).get
    }

    "raise WrongCredentials exception because of wrong password" in {
      (userServiceMock.getByEmail _).expects(incorrectCredentials.email).returns(Task.now(Some(activeUser)))
      a[WrongCredentials.type] should be thrownBy service.signIn(incorrectCredentials).get
    }

    "raise UserIsNotActive exception" in {
      (userServiceMock.getByEmail _).expects(correctCredentials.email).returns(Task.now(Some(inactiveUser)))
      a[UserIsNotActive.type] should be thrownBy service.signIn(correctCredentials).get
    }

    "return Task with user id" in {
      (userServiceMock.getByEmail _).expects(correctCredentials.email).returns(Task.now(Some(activeUser)))
      service.signIn(correctCredentials).get
    }
  }

}
