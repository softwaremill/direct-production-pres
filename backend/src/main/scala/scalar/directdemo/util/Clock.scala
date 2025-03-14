package scalar.directdemo.util

import java.time.Instant

trait Clock:
  def now(): Instant

object DefaultClock extends Clock:
  override def now(): Instant = java.time.Clock.systemUTC().instant()
