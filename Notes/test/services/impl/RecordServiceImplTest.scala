package services.impl

import java.time.Instant

import daos.{RecordDAO, UserDAO}
import exceptions.Exceptions.{ForbiddenException, NotFoundException}
import models.Record
import models.dtos.RecordDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import services.helpers.TimeHelper
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class RecordServiceImplTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val recordDAOMock = mock[RecordDAO]
  private val recordDTOMock = mock[RecordDTO]
  private val userDAOMock = mock[UserDAO]
  private val service = new RecordServiceImpl(recordDAOMock, userDAOMock)

  val recordDTO: RecordDTO = RecordDTO("Lorem", "ipsum", false)
  lazy val record: Record = recordDTO.toRecord(activeUser.id)

  "create" should {
    "return chart" in {
      (userDAOMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordDTOMock.toRecord _).expects(activeUser.id).returns(record)
      (recordDAOMock.create _).expects(record).returns(Task.now(None))
      service.create(activeUser, recordDTOMock).get mustBe None
    }

  }

  "getById" should {
    "return chart by id" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(Some(record)))
      service.getById(record._id.toString).get mustBe record
    }

    "raise NotFoundException" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(None))
      a[NotFoundException] should be thrownBy service.getById(record._id.toString).get
    }

  }

  "getByUser" should {
    "return user's charts" in {
      (userDAOMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordDAOMock.getByUser _).expects(activeUser.id).returns(Task.now(Seq(record)))
      service.getByUser(activeUser.id).get mustBe Seq(record)
    }

    "return user have no charts" in {
      (userDAOMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordDAOMock.getByUser _).expects(activeUser.id).returns(Task.now(Seq()))
      service.getByUser(activeUser.id).get mustBe Seq()
    }

    "raise NotFoundException because user does not exist" in {
      (userDAOMock.getById _).expects(inactiveUser.id).returns(Task.now(None))
      a[NotFoundException] should be thrownBy service.getByUser(inactiveUser.id).get
    }
  }

  "update" should {
    "return true" in {
      (recordDAOMock.update _).expects(record._id, record).returns(Task.now(Some(record)))
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(Some(record)))
      service.update(activeUser, recordDTO, record._id.toString).get mustBe true
    }

    "user tries to change foreign chart" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(Some(record)))
      a[ForbiddenException] should be thrownBy service.update(foreignUser, recordDTO, record._id.toString).get
    }

    "chart is not exist" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(None))
      a[NotFoundException] should be thrownBy service.update(activeUser, recordDTO, record._id.toString).get
    }
  }

  "delete" should {
    "return true" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(Some(record)))
      (recordDAOMock.delete _).expects(record._id).returns(Task.now(Some(record)))
      service.delete(activeUser, record._id.toString).get mustBe true
    }

    "user tries to delete foreign chart" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(Some(record)))
      a[ForbiddenException] should be thrownBy service.delete(foreignUser, record._id.toString).get
    }

    "chart is not exist" in {
      (recordDAOMock.getById _).expects(record._id).returns(Task.now(None))
      a[NotFoundException] should be thrownBy service.delete(activeUser, record._id.toString).get
    }
  }

}
