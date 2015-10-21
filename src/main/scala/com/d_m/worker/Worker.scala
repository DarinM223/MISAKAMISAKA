package com.d_m.worker

import akka.actor.ActorSystem

/**
 * Created by darin on 10/20/15.
 */
object Worker extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()

  // port is the first command-line argument
  val port = args(0).toInt

  // Start actor at port
  val configStr =
    """
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
    }
    """.stripMargin

  val system = ActorSystem("Worker", ConfigFactory.parseString(configStr))

  val dnsCacheConfig = config.getConfig("DNSCache")
  val dnsHostname = dnsCacheConfig.getString("akka.remote.netty.tcp.hostname")
  val dnsPort = dnsCacheConfig.getInt("akka.remote.netty.tcp.port")

  // Connect to dns cache remote actor
  val dnsCacheActor = system.actorSelection("akka.tcp://DNSCache@" + dnsHostname + ":" +
    dnsPort + "/user/DNSCacheActor")
}
