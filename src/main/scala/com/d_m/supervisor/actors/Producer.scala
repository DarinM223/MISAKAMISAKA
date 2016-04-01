package com.d_m.supervisor.actors

import akka.actor.Actor

/**
 * Created by darin on 10/26/15.
 */
class Producer extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case _ => None
  }
}
