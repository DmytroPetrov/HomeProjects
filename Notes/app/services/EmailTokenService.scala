package services

import models.EmailToken
import monix.eval.Task

trait EmailTokenService {

  def create(userId: Long): Task[EmailToken]

  def getByToken(token: String): Task[Option[EmailToken]]

  def getByUserId(id: Long): Task[Option[EmailToken]]

  def delete(emailToken: EmailToken): Task[Unit]

  def isExpired(emailToken: EmailToken): Boolean

}
