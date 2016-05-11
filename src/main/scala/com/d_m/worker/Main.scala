package com.d_m.worker

import akka.actor.ActorSystem

/**
  * Main application for a worker program
  */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()

  // port is the first command-line argument
  if (args.length <= 0) {
    println("ERROR: You need to specify a port")
  } else {
    val port = args(0).toInt

    // Start actor at port
    val configStr = """
    akka {
      actor {
        provider = "akka.remote.RemoteActorRefProvider"
      }
      remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "127.0.0.1"
          port = """.stripMargin + port + """
        }
      }
    }""".stripMargin

    val system = ActorSystem("Worker", ConfigFactory.parseString(configStr))

    val dnsResolverConfig = config.getConfig("DNSResolver")
    val dnsHostname =
      dnsResolverConfig.getString("akka.remote.netty.tcp.hostname")
    val dnsPort = dnsResolverConfig.getInt("akka.remote.netty.tcp.port")

    // Connect to dns cache remote actor
    val dnsResolverActor = system.actorSelection("akka.tcp://DNSResolver@" +
        dnsHostname + ":" + dnsPort + "/user/DNSResolverSupervisorActor")

    val rateLimiterConfig = config.getConfig("RateLimiter")
    val rateLimiterHostname =
      rateLimiterConfig.getString("akka.remote.netty.tcp.hostname")
    val rateLimiterPort =
      rateLimiterConfig.getInt("akka.remote.netty.tcp.port")

    val rateLimiterActor = system.actorSelection(
        "akka.tcp://RateLimiter@" + rateLimiterHostname + ":" +
        rateLimiterPort + "/user/RateLimiterSupervisorActor")
  }
}
