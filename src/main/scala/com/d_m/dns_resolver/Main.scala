package com.d_m.dns_resolver

import akka.actor.{Props, ActorSystem}
import com.d_m.dns_resolver.actors.DNSResolverSupervisor
import redis.RedisClient

/**
  * Main application for the DNS Resolver program
  */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  implicit val system = ActorSystem(
      "DNSResolver", config.getConfig("DNSResolver"))

  val redis = RedisClient()
  val supervisorActor = system.actorOf(
      Props(new DNSResolverSupervisor(redis)), "DNSResolverSupervisorActor")

  println(
      "DNS Resolver started at port: " +
      config.getConfig("DNSResolver").getInt("akka.remote.netty.tcp.port"))
}
