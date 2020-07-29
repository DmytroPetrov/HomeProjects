package controllers

import java.util.UUID

import controllers.filters.ControllerUtils
import javax.inject.Inject
import models.Record
import monix.execution.Scheduler
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{RecordService, UserService}

import scala.concurrent.{ExecutionContext, Future}

class RecordController @Inject()(cc:ControllerComponents, userService: UserService, recordService: RecordService)
                                (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc, userService) {
  def create() = Action.async(parse.json) {
    _.body.validate[Record].map { rec =>
      recordService.create(rec).map { _ => Created}
    }.getOrElse(Future.successful(BadRequest("Invalid")))
  }

  def get(id: String) = Action.async {
    val uid = UUID.fromString(id)
    recordService.getById(uid).map{ mayRec =>
      mayRec.map { rec =>
        Ok(Json.toJson(rec))
      }.getOrElse(NotFound)
    }
  }

}
