package com.d_m.worker

import java.net.URL

/**
  * Contains all of the messages used by the worker to communicate with the supervisor
  */
object Message {
  sealed trait Message
  case class RateLimitFailed(url: URL) extends Message
}
