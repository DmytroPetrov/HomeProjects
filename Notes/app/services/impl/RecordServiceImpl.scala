package services.impl

import java.util.UUID

import javax.inject.Inject
import models.Record
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import services.RecordService

import scala.concurrent.{ExecutionContext, Future}

class RecordServiceImpl @Inject() (implicit ec: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi)
  extends RecordService {

  import reactivemongo.play.json.compat,
  compat.json2bson._

  private def recordCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("record"))

  override def create(record: Record): Future[WriteResult] = recordCollection.flatMap {
    _.insert.one(
      record.copy(_id = Some(UUID.randomUUID()))
    )
  }

  override def getById(id: UUID): Future[Option[Record]] = recordCollection.flatMap {
    _.find(BSONDocument("_id" -> id)).one[Record]
  }
  //TODO: remake to get user's records
  override def getByUser: Future[Seq[Record]] = recordCollection.flatMap {
    _.find(BSONDocument.empty).cursor[Record]().collect[Seq](20)
  }

  override def update(id: UUID, record: Record): Future[Option[Record]] = recordCollection.flatMap {
    _.findAndUpdate(
      selector = BSONDocument("_id" -> id),
      update = record.toBSONDocument,
      fetchNewObject = true).map(_.result[Record])
  }

  override def delete(id: UUID): Future[Option[Record]] = recordCollection.flatMap {
    _.findAndRemove(selector = BSONDocument("_id" -> id)).map(_.result[Record])
  }
}
