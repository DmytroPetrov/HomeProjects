package services.helpers

import java.time.Instant

import services.helpers.impl.RealTimeHelper

trait TimeHelper {
  def now(): Instant
}

object TimeHelper {

  implicit val realTimeHelper: TimeHelper = new RealTimeHelper

}