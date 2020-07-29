package daos.slick.helper

import java.time.Instant

import exceptions.Exceptions.DbResultException
import models.{EmailToken, User}
import slick.lifted
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext

class Tables(dbConfigProvider: DatabaseConfigProvider) {
  val conf = dbConfigProvider.get[JdbcProfile]

  import conf.profile.api._

  implicit class SafeDB(action: conf.profile.ProfileAction[Int, NoStream, Effect.Write]) {
    def checkRowsAffected(implicit ex: ExecutionContext):
    DBIOAction[Int, NoStream, Effect.Write with Effect.Transactional] = action.flatMap { updatedRows =>
      if (updatedRows == 0) DBIO.failed(DbResultException)
      else DBIO.successful(updatedRows)
    }.transactionally
  }

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def firstName: Rep[String] = column[String]("first_name")

    def lastName: Rep[String] = column[String]("last_name")

    def email: Rep[String] = column[String]("email")

    def password: Rep[String] = column[String]("password")

    def active: Rep[Boolean] = column[Boolean]("active")

    def created: Rep[Instant] = column[Instant]("created")

    def lastUpdated: Rep[Instant] = column[Instant]("last_updated")

    def * : ProvenShape[User] =
      (id, firstName, lastName, email, password, active, created, lastUpdated) <> (User.tupled, User.unapply)
  }

  val users = lifted.TableQuery[UserTable]

  class EmailTokenTable(tag: Tag) extends Table[EmailToken](tag, "email_token") {

    def userId: Rep[Long] = column[Long]("user_id")

    def token: Rep[String] = column[String]("token")

    def created: Rep[Instant] = column[Instant]("created")

    def * : ProvenShape[EmailToken] = (userId, token, created) <> (EmailToken.tupled, EmailToken.unapply)

  }

  val emailTokens = lifted.TableQuery[EmailTokenTable]
}
