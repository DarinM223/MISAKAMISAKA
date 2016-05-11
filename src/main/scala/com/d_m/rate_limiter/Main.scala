package com.d_m.rate_limiter

import akka.actor.{Props, ActorSystem}
import com.d_m.rate_limiter.actors.RateLimiterSupervisor
import redis.RedisClient

/**
  * Created by darin on 10/22/15.
  */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  implicit val system = ActorSystem(
      "RateLimiter", config.getConfig("RateLimiter"))

  val redis = RedisClient()
  val supervisorActor = system.actorOf(
      Props(new RateLimiterSupervisor(redis)), "RateLimiterSupervisorActor")

  println(
      "Rate Limiter started at port: " +
      config.getConfig("RateLimiter").getInt("akka.remote.netty.tcp.port"))
}
