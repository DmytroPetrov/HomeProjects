package controllers.filters

import exceptions.Exceptions.{ForbiddenException, NoFilePassed, NoJsonException, NoMultipartFormData, NoStringException, NotFoundException, TokenBrokenOrExpired, UnauthorizedException, UserAlreadyExist, WrongCredentials, WrongJsonException, WrongParameterException}
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
