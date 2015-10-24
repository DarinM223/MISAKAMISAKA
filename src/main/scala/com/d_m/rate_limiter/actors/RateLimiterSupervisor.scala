package com.d_m.rate_limiter.actors

import java.net.URL

import akka.actor.{Actor, Props}
import redis.RedisClient

/**
 * Created by darin on 10/22/15.
 */
class RateLimiterSupervisor extends Actor {
  val redis = RedisClient()
  val rateLimiterActor = context.actorOf(Props(new RateLimiter(redis)), "RateLimiterActor")

  def receive = {
    case (url: URL, maxNumOfCalls: Int) => rateLimiterActor ! (sender(), url, maxNumOfCalls)
    case url: URL => rateLimiterActor ! (sender(), url)
  }
}