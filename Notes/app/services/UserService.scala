package services

import models.User
import models.dtos.CreateUserDTO
import monix.eval.Task
import services.helpers.TimeHelper

trait UserService {
  def update(currentUser: User, user: User): Task[Boolean]

  def create(createUserDTO: CreateUserDTO)(implicit th: TimeHelper): Task[User]

  def activateUser(userId: Long)(implicit th: TimeHelper): Task[Unit]

  def getById(userId: Long): Task[Option[User]]

  def getByEmail(email: String): Task[Option[User]]
}
