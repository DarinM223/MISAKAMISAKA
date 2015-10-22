package com.d_m.dns_resolver

import java.net.{URL, UnknownHostException}

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{OneForOneStrategy, Props, Actor}
import redis.RedisClient

/**
 * Created by darin on 10/21/15.
 */
class DNSResolverSupervisorActor extends Actor {
  import scala.concurrent.duration._

  val redis = RedisClient()

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: UnknownHostException => Stop
      case _ => Restart
    }

  def receive = {
    case url: URL =>
      val dnsResolverActor = context.actorOf(Props(new DNSResolverActor(sender(), redis)), "DNSResolverActor")
      dnsResolverActor ! url
  }
}
