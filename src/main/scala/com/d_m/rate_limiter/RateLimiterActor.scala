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
  val MAX_TIME_DIFFERENCE = 1000

  def receive = {
    // Save configuration for the maximum number of calls per second to call a domain
    case (sender: ActorRef, url: URL, maxNumCalls: Int) =>
      val host = url.getHost
      redis.set("config:" + host, maxNumCalls) andThen {
        case Success(success) if success =>
          sender ! RateLimiterMessage.ConfigSaved
        case _ =>
          sender ! RateLimiterMessage.ConfigFailed
      }
    // Check rate limit for the domain of a url
    case (sender: ActorRef, url: URL) =>
      val host = url.getHost
      redis.get("config:" + host) andThen {
        case Success(Some(data: ByteString)) =>
          val maxNumCalls: Int = RedisProtocolReply.decodeInteger(data) match {
            case Some((i, _)) => i.toInt
            case None => Int.MaxValue
          }
          val transaction = redis.multi()
          val currentTime = System.currentTimeMillis()

          RateLimiterRedis.removeExpiredKeys(transaction, host, currentTime, MAX_TIME_DIFFERENCE)
          transaction.zadd(host, currentTime.toDouble -> currentTime)
          transaction.zcount(host)
          transaction.exec() andThen {
            case Success(MultiBulk(Some(responses))) =>
              val setSize = responses.last.asInstanceOf[Long]
              if (setSize > maxNumCalls) {
                sender ! RateLimiterMessage.CannotCall
              } else {
                sender ! RateLimiterMessage.CanCall
              }
            case Failure(e) =>
              sender ! RateLimiterMessage.RedisError(e)
          }
        case _ =>
          sender ! RateLimiterMessage.NoConfigError
      }
  }
}
