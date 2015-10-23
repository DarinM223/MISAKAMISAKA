package com.d_m.rate_limiter

import java.net.URL

import akka.actor.{Props, Actor}
import redis.RedisClient

/**
 * Created by darin on 10/22/15.
 */
class RateLimiterSupervisorActor extends Actor {
  val redis = RedisClient()
  val rateLimiterActor = context.actorOf(Props(new RateLimiterActor(redis)), "RateLimiterActor")

  def receive = {
    case (url: URL, maxNumOfCalls: Int) => rateLimiterActor ! (sender(), url, maxNumOfCalls)
    case url: URL => rateLimiterActor ! (sender(), url)
  }
}
