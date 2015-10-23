package com.d_m.rate_limiter

/**
 * Created by darin on 10/22/15.
 */
object RateLimiterMessage {
  trait Message

  // Messages for saving configs
  case object ConfigSaved extends Message
  case object ConfigFailed extends Message

  // Messages for checking of you can call a rate-limited function again
  case object CanCall extends Message
  case object CannotCall extends Message

  // Error messages
  case class RedisError(error: Throwable) extends Message
  case object NoConfigError extends Message
}
