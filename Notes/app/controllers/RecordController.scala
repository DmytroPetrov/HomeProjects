package controllers

import controllers.filters.ControllerUtils
import javax.inject.Inject
import models.dtos.RecordDTO
import monix.execution.Scheduler
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{RecordService, UserService}

import scala.concurrent.ExecutionContext

class RecordController @Inject()(cc:ControllerComponents, userService: UserService, recordService: RecordService)
                                (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc, userService) {
  def create: Action[AnyContent] = userActionWithJson(RecordDTO.form) { request =>
    recordService.create(request.user, request.parsedBody).map { rec =>
      Ok
    }
  }

  def getByUser(userId: Long): Action[AnyContent] = userAction {
    recordService.getByUser(userId).map { records =>
      Ok(JsArray(records.map(_.toJson)))
    }
  }

  def getById(id: String): Action[AnyContent] = userAction {
    recordService.getById(id).map { record =>
      Ok(record.toJson)
    }
  }

  def update(id: String): Action[AnyContent] = userActionWithJson(RecordDTO.form) { request =>
    recordService.update(request.user, request.parsedBody, id).map(_ => Ok)
  }

  def delete(id: String): Action[AnyContent] = userAction { request =>
    recordService.delete(request.user, id).map(_ => Ok)
  }

}
