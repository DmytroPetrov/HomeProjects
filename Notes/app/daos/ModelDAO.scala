package daos

import monix.eval.Task

trait ModelDAO[T] {
  def getAll: Task[Seq[T]]
  def create(model: T): Task[T]
  def getById(id: Long): Task[Option[T]]
  def update(model: T): Task[Int]
  def delete(id: Long): Task[Int]
}
