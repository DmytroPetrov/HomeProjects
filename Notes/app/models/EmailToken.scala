package models

import java.time.Instant

case class EmailToken(
                       userId: Long,
                       token: String,
                       created: Instant
                     )
object EmailToken {

  def tupled: ((Long, String, Instant)) => EmailToken = (this.apply _).tupled

}
