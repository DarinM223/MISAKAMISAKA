package com.d_m.rate_limiter.actors

import java.net.URL

import akka.actor.SupervisorStrategy.{Stop, Restart}
import akka.actor.{OneForOneStrategy, Actor, Props}
import com.d_m.RedisException
import redis.RedisClient

/**
 * Supervisor actor that manages a RateLimiter Actor
 */
class RateLimiterSupervisor(val redis: RedisClient) extends Actor {
  import scala.concurrent.duration._

  val rateLimiterActor = context.actorOf(Props(new RateLimiter(redis)), "RateLimiterActor")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: RedisException => Restart
      case _ => Stop
    }

  def receive = {
    case (url: URL, maxNumOfCalls: Int) => rateLimiterActor ! (sender(), url, maxNumOfCalls)
    case url: URL => rateLimiterActor ! (sender(), url)
  }
}
