package com.d_m.dns_resolver.actors

import java.net.URL

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Terminated, Actor, OneForOneStrategy, Props}
import akka.routing.{Router, ActorRefRoutee, RoundRobinRoutingLogic}
import redis.RedisClient

/**
 * Supervisor actor that manages one DNSResolver actor
 */
class DNSResolverSupervisor(val redis: RedisClient) extends Actor {
  import scala.concurrent.duration._

  // so that resolver names are unique
  private[this] var actorCount = 0

  def receive = {
    case url: URL =>
      val dnsResolver = context.actorOf(Props(new DNSResolver(redis)), "DNSResolverActor" + actorCount)
      actorCount += 1
      dnsResolver ! (sender(), url)
  }
}
