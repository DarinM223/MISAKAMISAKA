package com.d_m.rate_limiter

import akka.actor.{Props, ActorSystem}

/**
 * Created by darin on 10/22/15.
 */
object RateLimiter extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("RateLimiter", config.getConfig("RateLimiter"))

  val supervisorActor = system.actorOf(Props[RateLimiterSupervisorActor], "RateLimiterSupervisorActor")
  println("Rate Limiter started at port: " + config.getConfig("RateLimiter").getInt("akka.remote.netty.tcp.port"))
}