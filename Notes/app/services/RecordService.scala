package services

import java.util.UUID

import models.Record
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait RecordService {

  def create(record: Record): Future[WriteResult]

  def getById(id: UUID): Future[Option[Record]]

  def getByUser: Future[Seq[Record]]

  def update(id: UUID, record: Record): Future[Option[Record]]

  def delete(id: UUID): Future[Option[Record]]
}
