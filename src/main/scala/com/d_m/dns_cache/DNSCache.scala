package com.d_m.dns_cache

import akka.actor.{Props, ActorSystem}

/**
 * Created by darin on 10/20/15.
 */
object DNSCache extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("DNSCache", config.getConfig("DNSCache"))

  val cacheActor = system.actorOf(Props[DNSCacheActor], "DNSCacheActor")
  println("DNS Cache started at port: " + config.getInt("akka.remote.netty.tcp.port"))
}
