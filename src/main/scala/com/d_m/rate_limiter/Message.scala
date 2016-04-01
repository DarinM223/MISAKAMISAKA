package com.d_m.rate_limiter

/**
 * Contains all of the messages used by the rate limiter to communicate
 * with other actors
 */
object Message {
  sealed trait Message

  // Messages for saving configs
  case object ConfigSaved extends Message
  case object ConfigFailed extends Message

  // Messages for checking of you can call a rate-limited function again
  case object CanCall extends Message
  case object CannotCall extends Message
}
