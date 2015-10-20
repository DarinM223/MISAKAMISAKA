package com.d_m.worker

import akka.actor._

/**
 * Created by darin on 10/20/15.
 */
class WorkerActor extends Actor {
  def receive = {
    case url: String =>
      sender ! List("TODO: Send back a list of URLs")
  }
}
