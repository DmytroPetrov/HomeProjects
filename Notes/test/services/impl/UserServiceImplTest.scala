package services.impl

import java.time.Instant

import daos.UserDAO
import exceptions.Exceptions.NotFoundException
import models.dtos.CreateUserDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import services.helpers.TimeHelper
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class UserServiceImplTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val userDAOMock = mock[UserDAO]
  private val userDTOMock = mock[CreateUserDTO]

  private val service = new UserServiceImpl(userDAOMock)

  "create" should {
    "return Task[User]" in {
      (userDTOMock.toUser()(_: TimeHelper)).expects(th).returns(inactiveUser)
      (userDAOMock.create _).expects(inactiveUser).returns(Task.now(inactiveUser))
      service.create(userDTOMock).get mustBe inactiveUser
    }

  }

  "activateUser" should {
    "return Task[Unit]" in {
      (userDAOMock.getById _).expects(inactiveUser.id).returns(Task.now(Some(inactiveUser)))
      (userDAOMock.update _).expects(activeUser).returns(Task.now(1))
      service.activateUser(inactiveUser.id).get mustBe (): Unit
    }


    "return NotFoundException()" in {
      (userDAOMock.getById _).expects(inactiveUser.id).returns(Task.now(None))
      a[NotFoundException] should be thrownBy service.activateUser(inactiveUser.id).get
    }

  }

}
