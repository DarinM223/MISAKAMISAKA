package com.d_m.rate_limiter

import java.net.URL

import akka.util.ByteString
import redis.protocol.{RedisProtocolReply, MultiBulk}

import scala.util.{Success, Failure}
import akka.actor.{ActorRef, Actor}
import redis.RedisClient
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by darin on 10/22/15.
 */
class RateLimiterActor(redis: RedisClient) extends Actor {


  def receive = {
    // Save configuration for the maximum number of calls per second to call a domain
    case (sender: ActorRef, url: URL, maxNumCalls: Int) =>
      val result = RateLimiterRedis.saveMaxNumberOfCalls(redis, url, maxNumCalls)

      result onSuccess { case message =>
        sender ! message
      }

      result onFailure { case _ =>
        sender ! RateLimiterMessage.ConfigFailed
      }
    // Check rate limit for the domain of a url
    case (sender: ActorRef, url: URL) =>
      val result = RateLimiterRedis.checkRateLimit(redis, url)

      result onSuccess { case message =>
        sender ! message
      }
  }
}
