package services.impl

import daos.EmailTokenDAO
import javax.inject.Inject
import models.EmailToken
import monix.eval.Task
import play.api.Configuration
import services.EmailTokenService
import services.helpers.{TimeHelper, TokenHelper}

class EmailTokenServiceImpl @Inject()
(emailTokenDAO: EmailTokenDAO, tokenHelper: TokenHelper, timeHelper: TimeHelper, config: Configuration)
  extends EmailTokenService {

  override def create(userId: Long): Task[EmailToken] = {
    val emailToken = EmailToken(userId, tokenHelper.generateToken(), timeHelper.now())
    emailTokenDAO.create(emailToken)
  }

  override def getByToken(token: String): Task[Option[EmailToken]] = emailTokenDAO.getByToken(token)

  override def delete(emailToken: EmailToken): Task[Unit] = emailTokenDAO.delete(emailToken.userId).map(_ => ())

  override def isExpired(emailToken: EmailToken): Boolean = {
    val expirationTime = config.get[Long]("emailTokenTTl")
    emailToken.created.isBefore(timeHelper.now().minusMillis(expirationTime))
  }

  override def getByUserId(id: Long): Task[Option[EmailToken]] = emailTokenDAO.getById(id)

}

