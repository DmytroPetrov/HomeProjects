package services.helpers.impl

import java.time.Instant

import services.helpers.TimeHelper

class RealTimeHelper extends TimeHelper {
  override def now(): Instant = Instant.now()
}
