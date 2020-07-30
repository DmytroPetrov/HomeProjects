package services

import java.util.UUID

import models.{Record, User}
import models.dtos.RecordDTO
import monix.eval.Task
import reactivemongo.api.commands.WriteResult

trait RecordService {

  def getByUser(userId: Long): Task[Seq[Record]]

  def create(owner: User, recordDTO: RecordDTO): Task[Option[Int]]

  def getById(recordId: String): Task[Record]

  def update(currentUser: User, recordDTO: RecordDTO, recordId: String): Task[Boolean]

  def delete(currentUser: User, recordId: String): Task[Boolean]

}
