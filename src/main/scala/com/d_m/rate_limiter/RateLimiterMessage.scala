package com.d_m.rate_limiter

/**
 * Created by darin on 10/22/15.
 */
object RateLimiterMessage {
  trait RateLimiterMessage

  // Messages for saving configs
  case object ConfigSaved extends RateLimiterMessage
  case object ConfigFailed extends RateLimiterMessage

  // Messages for checking of you can call a rate-limited function again
  case object CanCall extends RateLimiterMessage
  case object CannotCall extends RateLimiterMessage

  // Error messages
  case class RedisError(error: Throwable) extends RateLimiterMessage
  case object NoConfigError extends RateLimiterMessage
}
