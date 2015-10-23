package com.d_m.dns_resolver

import akka.actor.{Props, ActorSystem}

/**
 * Created by darin on 10/20/15.
 */
object DNSResolver extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("DNSResolver", config.getConfig("DNSResolver"))

  val supervisorActor = system.actorOf(Props[DNSResolverSupervisorActor], "DNSResolverSupervisorActor")
  println("DNS Resolver started at port: " + config.getConfig("DNSResolver").getInt("akka.remote.netty.tcp.port"))
}
