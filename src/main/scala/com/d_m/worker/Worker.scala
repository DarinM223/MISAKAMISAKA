package com.d_m.worker

import akka.actor.ActorSystem

/**
 * Created by darin on 10/20/15.
 */
object Worker extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("Worker", config.getConfig("Worker"))

  val dnsCacheConfig = config.getConfig("DNSCache")
  val dnsHostname = dnsCacheConfig.getString("akka.remote.netty.tcp.hostname")
  val dnsPort = dnsCacheConfig.getInt("akka.remote.netty.tcp.port")

  // Connect to dns cache remote actor
  val dnsCacheActor = system.actorSelection("akka.tcp://DNSCache@" + dnsHostname + ":" +
    dnsPort + "/user/DNSCacheActor")

}
