package services.impl

import java.util.UUID

import daos.{RecordDAO, UserDAO}
import exceptions.Exceptions.{ForbiddenException, NotFoundException, UnauthorizedException}
import javax.inject.Inject
import models.{Record, User}
import models.dtos.RecordDTO
import monix.eval.Task
import reactivemongo.api.commands.WriteResult
import services.RecordService

class RecordServiceImpl @Inject()(recordDAO: RecordDAO, userDAO: UserDAO) extends RecordService {

  override def create(owner: User, recordDTO: RecordDTO): Task[Option[Int]] = ifActiveUserExist(owner.id).flatMap {
    case false => Task.raiseError(UnauthorizedException)
    case true => recordDAO.create(recordDTO.toRecord(owner.id))
  }

  override def getByUser(userId: Long): Task[Seq[Record]] = ifActiveUserExist(userId).flatMap {
    case false => Task.raiseError(NotFoundException("Record", s"with userId = $userId"))
    case true => recordDAO.getByUser(userId)
  }

  override def getById(recordId: UUID): Task[Record] = get(recordId)

  override def update(currentUser: User, recordDTO: RecordDTO, recordId: UUID): Task[Boolean] =
    ifExistAndOwner(currentUser.id, recordId) {
      existing => recordDAO.update(recordId, existing.updateFrom(recordDTO.toRecord(currentUser.id))).map(_ => true)
    }

  override def delete(currentUser: User, recordId: UUID): Task[Boolean] = ifExistAndOwner(currentUser.id, recordId) {
    _ => recordDAO.delete(recordId).map(_ => true)
  }

  private def ifActiveUserExist(id: Long): Task[Boolean] = {
    userDAO.getById(id).map {
      case None => false
      case Some(user) if !user.active => false
      case _ => true
    }
  }

  private def get(recordId: UUID): Task[Record] = recordDAO.getById(recordId).flatMap {
    case None => Task.raiseError(NotFoundException("Record", s"with id = $recordId"))
    case Some(rec) => Task.now(rec)
  }

  private def ifExistAndOwner[T](ownerId: Long, chartId: UUID)(block: Record => Task[T]): Task[T] =
    get(chartId).flatMap {
      case rec if rec.ownerId != ownerId => throw ForbiddenException("No rights to change record")
      case rec => block(rec)
    }


}
