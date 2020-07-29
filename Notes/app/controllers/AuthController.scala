package controllers

import controllers.filters.{ControllerUtils, CustomRequest}
import javax.inject.Inject
import models.dtos.{CreateUserDTO, Credentials}
import monix.execution.Scheduler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, UserService}

import scala.concurrent.ExecutionContext

class AuthController @Inject()(cc: ControllerComponents,
                               userService: UserService,
                               authService: AuthService)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc, userService){

  def signIn: Action[AnyContent] = actionWithJson(Credentials.form) { request =>
    authService.signIn(request.parsedBody).map(userId =>
      Ok.withSession(CustomRequest.CookieUserId -> userId.toString)
    )
  }

  def signOut: Action[AnyContent] = Action.apply {
    Ok.withNewSession
  }

  //TODO: make usage of forms simpler by action composition.
  def signUp: Action[AnyContent] = actionWithJson(CreateUserDTO.form){
    r => authService.signUp(r.parsedBody).map(_ => Ok)
  }

  def confirm(token: String): Action[AnyContent] = simpleAction { implicit request =>
    authService.confirm(token).map(_ => Ok)
  }

}
