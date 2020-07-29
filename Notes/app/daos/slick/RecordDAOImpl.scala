package daos.slick

import java.util.UUID

import daos.RecordDAO
import javax.inject.Inject
import models.Record
import monix.eval.Task
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}

class RecordDAOImpl @Inject()(implicit ec: ExecutionContext,
                              reactiveMongoApi: ReactiveMongoApi)
  extends RecordDAO {

  import reactivemongo.play.json.compat
  import daos.slick.helper.Helpers._
  import compat.json2bson._

  private def recordCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("record"))

  override def create(record: Record): Task[WriteResult] = recordCollection.flatMap {
    _.insert.one(
      record.copy(_id = UUID.randomUUID())
    )
  }.wrapEx

  override def getById(id: UUID): Task[Option[Record]] = recordCollection.flatMap {
    _.find(BSONDocument("_id" -> id)).one[Record]
  }.wrapEx

  override def getByUser(userId: Long): Task[Seq[Record]] = recordCollection.flatMap {
    _.find(BSONDocument("ownerId" -> userId)).cursor[Record]().collect[Seq](20)
  }.wrapEx

  override def update(id: UUID, record: Record): Task[Option[Record]] = recordCollection.flatMap {
    _.findAndUpdate(
      selector = BSONDocument("_id" -> id),
      update = record.toBSONDocument,
      fetchNewObject = true).map(_.result[Record])
  }.wrapEx

  override def delete(id: UUID): Task[Option[Record]] = recordCollection.flatMap {
    _.findAndRemove(selector = BSONDocument("_id" -> id)).map(_.result[Record])
  }.wrapEx
}
