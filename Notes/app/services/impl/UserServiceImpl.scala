package services.impl

import daos.UserDAO
import exceptions.Exceptions.NotFoundException
import javax.inject.Inject
import models.User
import models.dtos.CreateUserDTO
import monix.eval.Task
import services.UserService
import services.helpers.TimeHelper

class UserServiceImpl @Inject()(userDAO: UserDAO) extends UserService {

  override def update(currentUser: User, user: User): Task[Boolean] = userDAO.update(currentUser.updateFrom(user)).map(_ => true)

  private def get(userId: Long): Task[User] = userDAO.getById(userId).map {
    case None => throw NotFoundException("User", s"id = $userId")
    case Some(existingRec) => existingRec
  }

  def getById(userId: Long): Task[Option[User]] = userDAO.getById(userId)

  override def create(createUserDTO: CreateUserDTO)(implicit th: TimeHelper): Task[User] = userDAO.create(createUserDTO.toUser())

  override def activateUser(userId: Long)(implicit th: TimeHelper): Task[Unit] = {
    get(userId).flatMap{ user =>
      val activeUser = user.copy(active = true, lastUpdated = th.now())
      userDAO.update(activeUser).map(_ => ())
    }
  }

  override def getByEmail(email: String): Task[Option[User]] = userDAO.getByEmail(email)
}