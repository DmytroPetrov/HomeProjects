package daos

import java.util.UUID

import models.Record
import monix.eval.Task
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait RecordDAO {

  def create(record: Record): Task[Option[Int]]

  def getById(id: UUID): Task[Option[Record]]

  def getByUser(userId: Long): Task[Seq[Record]]

  def update(id: UUID, record: Record): Task[Option[Record]]

  def delete(id: UUID): Task[Option[Record]]
}
