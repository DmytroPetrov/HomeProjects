package services.impl

import java.time.Instant

import daos.EmailTokenDAO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import services.helpers.{TimeHelper, TokenHelper}
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class EmailTokenServiceImplTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val emailTokenDAOMock = mock[EmailTokenDAO]
  private val timeHelperMock = mock[TimeHelper]
  private val tokenHelperMock = mock[TokenHelper]

  private val conf: Configuration = Configuration.from(Map("emailTokenTTl" -> 1))



  private val service = new EmailTokenServiceImpl(emailTokenDAOMock, tokenHelperMock, timeHelperMock, conf)

  "create" should {

    "return EmailToken" in {
      (tokenHelperMock.generateToken _).expects().returns(emailToken.token)
      (timeHelperMock.now _).expects().returns(emailToken.created)
      (emailTokenDAOMock.create _).expects(emailToken).returns(Task.now(emailToken))
      val result = service.create(inactiveUser.id).get
      result mustBe emailToken
    }

  }

  "getByToken" should {

    "return Task[Some[EmailToken]]" in {
      (emailTokenDAOMock.getByToken _).expects(emailToken.token).returns(Task.now(Some(emailToken)))
      val result = service.getByToken(emailToken.token).get
      result.get mustBe emailToken
    }

  }

  "delete" should {

    "return Task[Unit]" in {
      (emailTokenDAOMock.delete _).expects(emailToken.userId).returns(Task.now(1))
      service.delete(emailToken).get mustBe (): Unit
    }

  }

  "isExpired" should {

    "return Task[Boolean] with true value" in {
      (timeHelperMock.now _).expects().returns(Instant.MAX)
      service.isExpired(emailToken) mustBe true
    }

    "return Task[Boolean] with false value" in {
      (timeHelperMock.now _).expects().returns(Instant.MIN.plusMillis(conf.get[Long]("emailTokenTTl")))
      service.isExpired(emailToken) mustBe false
    }

  }

  "getByUserId" should {

    "return Task with Some(EmailToken) value" in {
      (emailTokenDAOMock.getById _).expects(emailToken.userId).returns(Task.now(Some(emailToken)))
      service.getByUserId(emailToken.userId).get mustBe Some(emailToken)
    }

    "return Task with None value" in {
      (emailTokenDAOMock.getById _).expects(emailToken.userId).returns(Task.now(None))
      service.getByUserId(emailToken.userId).get mustBe None
    }

  }

}

