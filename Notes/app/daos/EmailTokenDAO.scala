package daos

import models.EmailToken
import monix.eval.Task

trait EmailTokenDAO extends ModelDAO[EmailToken] {

  def getByToken(token: String): Task[Option[EmailToken]]

}
