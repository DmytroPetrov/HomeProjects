package controllers

import java.time.Instant

import exceptions.Exceptions.{TokenBrokenOrExpired, UserAlreadyExist, UserIsNotActive, WrongCredentials}
import models.dtos.CreateUserDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AuthService, UserService}
import services.helpers.TimeHelper
import testUtils.{AsyncUtils, TestDataUtils}

import scala.concurrent.ExecutionContext

class AuthControllerTest extends PlaySpec with MockFactory with AsyncUtils with TestDataUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  private val userServiceMock = mock[UserService]
  private val authServiceMock = mock[AuthService]
  private val controller = new AuthController(stubControllerComponents(), userServiceMock, authServiceMock)

  private def fakeSignUpRequest(firstName: String, lastName: String, email: String, password: String): FakeRequest[AnyContent] = {
    val jsBody = JsObject(Map(
      "firstName" -> JsString(firstName),
      "lastName" -> JsString(lastName),
      "email" -> JsString(email),
      "password" -> JsString(password)
    ))
    FakeRequest().withBody(AnyContent(jsBody))
  }

  private def fakeRequestFromDTO(user: CreateUserDTO): FakeRequest[AnyContent] = fakeSignUpRequest(user.firstName, user.lastName, user.email, user.password)

  private def fakeSignInRequest(email: String, password: String): FakeRequest[AnyContent] = {
    val jsBody = JsObject(Map("email" -> JsString(email), "password" -> JsString(password)))
    FakeRequest().withBody(AnyContent(jsBody))
  }

  "signUp" should {

    "return Ok" in {
      val request = fakeRequestFromDTO(correctUserDTO)
      (authServiceMock.signUp _).expects(correctUserDTO).returns(Task.now(()))
      val result = controller.signUp.apply(request)
      result.get mustBe Ok
    }

    "return UnprocessableEntity(Wrong json: $formWithErrors)" in {
      val request = fakeSignUpRequest("Lorem", "", "dolorsitamet", "con")
      val result = controller.signUp.apply(request)
      val formWithErrors = CreateUserDTO.form.bindFromRequest()(request)
      result.get mustBe UnprocessableEntity(s"Wrong json: $formWithErrors")
    }

    "return Conflict(User already exist)" in {
      val request = fakeRequestFromDTO(correctUserDTO)
      (authServiceMock.signUp _).expects(correctUserDTO).returns(Task.raiseError(UserAlreadyExist()))
      val result = controller.signUp.apply(request)
      result.get mustBe Conflict("User already exist")
    }

    "return InternalServerError(Internal server error)" in {
      val request = fakeRequestFromDTO(correctUserDTO)
      (authServiceMock.signUp _).expects(correctUserDTO).returns(Task.raiseError(new RuntimeException))
      val result = controller.signUp.apply(request)
      result.get mustBe InternalServerError
    }

  }

  private val token = "token"

  "confirm" should {

    "return Ok" in {
      (authServiceMock.confirm _).expects(token).returns(Task.now(()))
      val result = controller.confirm(token).apply(FakeRequest())
      result.get mustBe Ok
    }

    "return NotAcceptable(Token expired or broken)" in {
      (authServiceMock.confirm _).expects(token).returns(Task.raiseError(TokenBrokenOrExpired))
      val result = controller.confirm(token).apply(FakeRequest())
      result.get mustBe NotAcceptable("Token expired or broken")
    }

    "return InternalServerError(Internal server error)" in {
      (authServiceMock.confirm _).expects(token).returns(Task.raiseError(new RuntimeException("")))
      val result = controller.confirm(token).apply(FakeRequest())
      result.get mustBe InternalServerError
    }

  }

  "signIn" should {

    "return Ok" in {
      val request = fakeSignInRequest(correctCredentials.email, correctCredentials.password)
      (authServiceMock.signIn _).expects(correctCredentials).returns(Task.now(activeUser.id))
      val result = controller.signIn().apply(request)
      result.get mustBe Ok.withSession("userId" -> activeUser.id.toString)
    }

    "return ExpectationFailed" in {
      val request = fakeSignInRequest(incorrectCredentials.email, incorrectCredentials.password)
      (authServiceMock.signIn _).expects(incorrectCredentials).returns(Task.raiseError(WrongCredentials))
      val result = controller.signIn().apply(request)
      result.get mustBe ExpectationFailed
    }

    "return 500 Internal Server Error" in {
      val request = fakeSignInRequest(incorrectCredentials.email, incorrectCredentials.password)
      (authServiceMock.signIn _).expects(incorrectCredentials).returns(Task.raiseError(UserIsNotActive))
      val result = controller.signIn().apply(request)
      result.get mustBe InternalServerError
    }
  }

}
