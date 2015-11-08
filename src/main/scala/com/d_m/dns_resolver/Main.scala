package com.d_m.dns_resolver

import akka.actor.{Props, ActorSystem}
import com.d_m.dns_resolver.actors.DNSResolverSupervisor

/**
 * Main application for the DNS Resolver program
 */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("DNSResolver", config.getConfig("DNSResolver"))

  val supervisorActor = system.actorOf(Props[DNSResolverSupervisor], "DNSResolverSupervisorActor")
  println("DNS Resolver started at port: " + config.getConfig("DNSResolver").getInt("akka.remote.netty.tcp.port"))
}
