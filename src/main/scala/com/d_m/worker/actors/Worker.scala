package com.d_m.worker.actors

import akka.actor._

/**
 * Created by darin on 10/20/15.
 */
class Worker extends Actor {
  def receive = {
    case url: String =>
      sender ! List("TODO: Send back a list of URLs")
  }
}
