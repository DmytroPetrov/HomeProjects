package controllers.filters

import controllers.filters.CustomRequest.MultipartFile
import exceptions.Exceptions.{ForbiddenException, NoFilePassed, NoJsonException, NoMultipartFormData, NoStringException, NotFoundException, TokenBrokenOrExpired, UnauthorizedException, UserAlreadyExist, UserIsNotActive, WrongCredentials, WrongJsonException, WrongParameterException}
import monix.eval.Task
import monix.execution.Scheduler
import play.api.data.Form
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext

abstract class ControllerUtils(cc: ControllerComponents, userService: UserService)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends AbstractController(cc) {


  def simpleAction(block: Request[AnyContent] => Task[Result]): Action[AnyContent] = Action.async { request =>
    block(request).onErrorRecover(recover).runToFuture
  }

  def simpleAction(asyncResult: Task[Result]): Action[AnyContent] = simpleAction(_ => asyncResult)


  def actionWithJson[A](form: Form[A])(block: CustomRequest[A] => Task[Result]): Action[AnyContent] = simpleAction { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        Task.now(UnprocessableEntity(s"Wrong json: $formWithErrors"))
      },
      model => block(CustomRequest(None, Some(model), request))
    )
  }


  def actionWithString(block: CustomRequest[String] => Task[Result]): Action[AnyContent] = simpleAction {
    request => requestToString(request).flatMap(model => block(CustomRequest[String](None, Some(model), request)))
  }


  def actionWithFile(fileKey: String)(block: CustomRequest[MultipartFile] => Task[Result]): Action[AnyContent] =
    simpleAction { request =>
      request.body.asMultipartFormData match {
        case Some(formData) => formData.file(fileKey) match {
          case Some(file) => block(CustomRequest(None, Some(file), request))
          case _ => Task.raiseError(NoFilePassed(fileKey))
        }

        case _ => Task.raiseError(NoMultipartFormData)
      }
    }


  def userAction(block: CustomRequest[Nothing] => Task[Result]): Action[AnyContent] = simpleAction { request =>
    request.session.get(CustomRequest.CookieUserId) match {
      case Some(id) => userService.getById(id.toLong).flatMap {
        case Some(user) if user.active => block(CustomRequest[Nothing](Some(user), None, request))
        case Some(user) if !user.active => Task.raiseError(UserIsNotActive)
        case _ => Task.raiseError(UnauthorizedException)
      }

      case _ => Task.raiseError(UnauthorizedException)
    }
  }


  def userAction(res: Task[Result]): Action[AnyContent] = userAction(_ => res)


  def userActionWithJson[A](form: Form[A])(block: CustomRequest[A] => Task[Result]): Action[AnyContent] = userAction { implicit request =>
    form.bindFromRequest()(request.request).fold(
      formWithErrors => Task.raiseError(WrongJsonException(formWithErrors.errors)),
      model => block(request.withParsedBody(model))
    )
  }


  def userActionWithString(block: CustomRequest[String] => Task[Result]): Action[AnyContent] =
    userAction { authReq =>
      requestToString(authReq.request).flatMap { body =>
        block(authReq.withParsedBody(body))
      }
    }


  def userActionWithFile(fileKey: String)(block: CustomRequest[MultipartFile] => Task[Result]): Action[AnyContent] =
    userAction { request =>
      request.request.body.asMultipartFormData match {
        case Some(formData) => formData.file(fileKey) match {
          case Some(file) => block(request.withParsedBody(file))
          case _ => Task.raiseError(NoFilePassed(fileKey))
        }

        case _ => Task.raiseError(NoMultipartFormData)
      }
    }

  private def requestToString(request: Request[AnyContent]): Task[String] = request.body.asText match {
    case Some(str) => Task.now(str)
    case _ => Task.raiseError(NoStringException)
  }

  private def recover: PartialFunction[Throwable, Result] = {

    //401
    case UnauthorizedException => Unauthorized

    //403
    case ForbiddenException(msg) => Forbidden(msg)

    //404
    case NotFoundException(msg) => NotFound(msg)

    //406
    case TokenBrokenOrExpired => NotAcceptable("Token expired or broken")

    //409
    case UserAlreadyExist(msg) => Conflict(msg)

    //422
    case WrongJsonException(e) => UnprocessableEntity("Wrong json " + e)

    case NoFilePassed(key: String) => UnprocessableEntity(s"No file with key=$key passed in multipart/form-data")



    //412
    case NoJsonException => PreconditionFailed("No json passed")

    case NoStringException => PreconditionFailed("No string passed")

    case NoMultipartFormData => PreconditionFailed("No multipart/form-data passed")

    case WrongParameterException(message: String) => PreconditionFailed("Not valid parameter" + message)


    //417
    case WrongCredentials => ExpectationFailed

    //500
    case ex: Throwable =>
      println(ex)
      ex.printStackTrace()
      InternalServerError
  }

}