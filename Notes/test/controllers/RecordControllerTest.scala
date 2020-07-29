package controllers

import java.time.Instant
import java.util.UUID

import models.{Record, User}
import models.dtos.RecordDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsArray, JsBoolean, JsObject, JsString, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.helpers.TimeHelper
import services.{RecordService, UserService}
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class RecordControllerTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val userServiceMock = mock[UserService]
  private val recordServiceMock = mock[RecordService]
  private val recordDTOMock = mock[RecordDTO]
  private val controller = new RecordController(stubControllerComponents(), userServiceMock, recordServiceMock)

  val recordDTO: RecordDTO = RecordDTO("Lorem", "ipsum", false)
  lazy val record: Record = recordDTO.toRecord(activeUser.id)

  private def fakeCreateRequest(title: String,
                                text: String,
                                complete: Boolean): FakeRequest[AnyContent] = {
    val jsBody = JsObject(Map(
      "title" -> JsString(title),
      "text" -> JsString(text),
      "complete" -> JsBoolean(complete)
    ))

    FakeRequest().withBody(AnyContent(jsBody))
      .withSession("userId" -> activeUser.id.toString)
  }

  private  def fakeCreateRequestFromDTO(dto: RecordDTO): FakeRequest[AnyContent] =
    fakeCreateRequest(dto.title, dto.text, dto.complete)

  private def fakeChartRequest(record: Record, method: String = "POST"): FakeRequest[AnyContent] =
    FakeRequest().withBody(AnyContent(Json.toJson(record)))
      .withSession("userId" -> activeUser.id.toString).withMethod(method)

  private val emptyRequestWithSession: FakeRequest[AnyContent] = FakeRequest().withSession("userId" -> activeUser.id.toString)

  "create" should {
    "return Ok" in {
      val request = fakeCreateRequestFromDTO(recordDTO)
      (userServiceMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordServiceMock.create(_: User, _: RecordDTO))
        .expects(activeUser, recordDTO)
        .returns(Task.now(Some(1)))
      controller.create.apply(request).get mustBe Ok
    }
  }

  "getById" should {
    "return Ok(record)" in {
      (userServiceMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordServiceMock.getById _).expects(record._id).returns(Task.now(record))
      controller.getById(record._id.toString).apply(emptyRequestWithSession).get mustBe Ok(Json.toJson(record))
    }
  }

  "getByUser" should {
    "return Ok" in {
      (userServiceMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordServiceMock.getByUser _).expects(activeUser.id).returns(Task.now(Seq(record)))
      controller.getByUser(activeUser.id).apply(emptyRequestWithSession).get mustBe Ok(JsArray(Seq(Json.toJson(record))))
    }
  }

  "update" should {
    "return Ok" in {
      val request = fakeChartRequest(record, "PATCH")
      (userServiceMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordServiceMock.update(_: User, _: RecordDTO, _: UUID)).expects(activeUser, recordDTO, record._id).returns(Task.now(true))
      controller.update(record._id.toString).apply(request).get mustBe Ok
    }
  }

  "delete" should {
    "return Ok" in {
      (userServiceMock.getById _).expects(activeUser.id).returns(Task.now(Some(activeUser)))
      (recordServiceMock.delete(_: User, _: UUID)).expects(activeUser, record._id).returns(Task.now(true))
      controller.delete(record._id.toString).apply(emptyRequestWithSession).get mustBe Ok
    }
  }

}
