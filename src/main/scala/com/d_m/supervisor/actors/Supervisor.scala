package com.d_m.supervisor.actors

import akka.actor._

/**
 * Created by darin on 10/20/15.
 */
class Supervisor extends Actor {
  def receive = {
    case urls: List[String] =>
      println("TODO: do something with the list of urls")
  }
}
