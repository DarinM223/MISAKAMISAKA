package com.d_m.rate_limiter.actors

import java.net.URL

import akka.actor.{Actor, ActorRef}
import com.d_m.rate_limiter.{Message, RedisUtils}
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Actor that handles rate limits on domains
 * Allows you to set a rate limit and check if you can call a domain
 */
class RateLimiter(redis: RedisClient) extends Actor {
  def receive = {
    // Save configuration for the maximum number of calls per second to call a domain
    case (originalSender: ActorRef, url: URL, maxNumCalls: Int) =>
      val result = RedisUtils.saveMaxNumberOfCalls(redis, url, maxNumCalls)

      result onSuccess { case message =>
        originalSender ! message
      }

      result onFailure { case _ =>
        originalSender ! Message.ConfigFailed
      }
    // Check rate limit for the domain of a url
    case (originalSender: ActorRef, url: URL) =>
      val result = RedisUtils.checkRateLimit(redis, url)

      result onSuccess { case message =>
        originalSender ! message
      }

      result onFailure { case e =>
        originalSender ! None
      }
  }
}
