package com.d_m.dns_resolver

import java.net.UnknownHostException

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{OneForOneStrategy, Props, Actor}

/**
 * Created by darin on 10/21/15.
 */
class DNSResolverSupervisorActor extends Actor {
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: UnknownHostException => Stop
      case _ => Restart
    }

  def receive = {
    case url: String =>
      val dnsResolverActor = context.actorOf(Props(new DNSResolverActor(sender())), "DNSResolverActor")
      dnsResolverActor ! url
  }
}
