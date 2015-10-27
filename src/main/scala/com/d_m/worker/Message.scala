package com.d_m.worker

import java.net.URL

/**
 * Created by darin on 10/26/15.
 */
object Message {
  trait Message
  case class RateLimitFailed(url: URL) extends Message
}
