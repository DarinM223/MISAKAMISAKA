package com.d_m.supervisor.actors

import akka.actor._

/**
 * Supervisor that manages worker actors in a cluster
 */
class Supervisor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case urls: List[String] =>
      println("TODO: do something with the list of urls")
  }
}
