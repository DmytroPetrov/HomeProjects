package daos.slick

import daos.EmailTokenDAO
import daos.slick.helper.Tables
import javax.inject.Inject
import models.EmailToken
import monix.eval.Task
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class EmailTokenDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                 (implicit ex: ExecutionContext)
  extends Tables(dbConfigProvider) with EmailTokenDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import daos.slick.helper.Helpers._
  import profile.api._

  private val emailTokensQuery = emailTokens returning emailTokens

  override def getAll: Task[Seq[EmailToken]] = db.run(emailTokens.result).wrapEx

  override def create(emailToken: EmailToken): Task[EmailToken] = db.run(emailTokensQuery += emailToken).wrapEx

  override def getById(userId: Long): Task[Option[EmailToken]] = db.run(emailTokens.filter(_.userId === userId).result.headOption).wrapEx

  override def update(emailToken: EmailToken): Task[Int] = db.run(emailTokens.filter(_.userId === emailToken.userId).update(emailToken).checkRowsAffected).wrapEx

  override def delete(userId: Long): Task[Int] = db.run(emailTokens.filter(_.userId === userId).delete.checkRowsAffected).wrapEx

  override def getByToken(token: String): Task[Option[EmailToken]] = db.run(emailTokens.filter(_.token === token).result.headOption).wrapEx

}
