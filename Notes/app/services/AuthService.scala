package services

import models.dtos.{CreateUserDTO, Credentials}
import monix.eval.Task

trait AuthService {

  def confirm(token: String): Task[Unit]

  def signUp(user: CreateUserDTO): Task[Unit]

  def signIn(credentials: Credentials): Task[Long]

}
