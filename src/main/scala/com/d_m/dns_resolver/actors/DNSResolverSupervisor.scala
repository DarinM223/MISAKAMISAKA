package com.d_m.dns_resolver.actors

import java.net.URL

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, OneForOneStrategy, Props}
import com.d_m.RedisException
import redis.RedisClient

/**
  * Supervisor actor that manages one DNSResolver actor
  */
class DNSResolverSupervisor(val redis: RedisClient) extends Actor {
  import scala.concurrent.duration._

  // so that resolver names are unique
  private[this] var actorCount = 0

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: RedisException => Restart
      case _ => Stop
    }

  def receive: PartialFunction[Any, Unit] = {
    case url: URL =>
      val dnsResolver = context.actorOf(
          Props(new DNSResolver(redis)), "DNSResolverActor" + actorCount)
      actorCount += 1
      dnsResolver ! (sender(), url)
  }
}
