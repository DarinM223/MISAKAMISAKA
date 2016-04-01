package com.d_m.rate_limiter.actors

import java.net.URL

import akka.actor.{Actor, ActorRef}
import com.d_m.RedisException
import com.d_m.rate_limiter.RedisUtils
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Actor that handles rate limits on domains
 * Allows you to set a rate limit and check if you can call a domain
 */
class RateLimiter(redis: RedisClient) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    // Save configuration for the maximum number of calls per second to call a domain
    case (originalSender: ActorRef, url: URL, maxNumCalls: Int) =>
      val result = RedisUtils.saveMaxNumberOfCalls(redis, url, maxNumCalls)

      result onComplete {
        case Success(message) => originalSender ! message
        case Failure(e) => throw RedisException(e.getMessage)
      }
    // Check rate limit for the domain of a url
    case (originalSender: ActorRef, url: URL) =>
      val result = RedisUtils.checkRateLimit(redis, url)

      result onComplete {
        case Success(Some(message)) => originalSender ! message
        case Failure(e) => throw RedisException(e.getMessage)
        case _ => println("TODO: have to change Options to Trys in Redis code")
      }
  }
}
