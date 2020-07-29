package models

import java.time.Instant

import services.helpers.TimeHelper

case class User(
                 id: Long,
                 firstName: String,
                 lastName: String,
                 email: String,
                 password: String,
                 active: Boolean,
                 created: Instant,
                 lastUpdated: Instant
               ){

  def updateFrom(that: User)(implicit th: TimeHelper): User = copy(
    firstName = that.firstName,
    lastName = that.lastName,
    lastUpdated = th.now()
  )

}

object User {

  def tupled: ((Long, String, String, String, String, Boolean, Instant, Instant)) => User = (this.apply _).tupled

}
