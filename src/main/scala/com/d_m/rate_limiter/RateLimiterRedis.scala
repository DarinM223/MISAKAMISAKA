package com.d_m.rate_limiter

import java.net.URL

import akka.util.ByteString
import redis.{RedisClient, RedisCommands}
import redis.api.Limit
import redis.protocol.{MultiBulk, RedisProtocolReply}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by darin on 10/22/15.
 */
object RateLimiterRedis {
  val MAX_TIME_DIFFERENCE = 1000

  /**
   * Removes all expired keys from a time and a time range
   * @param redis the redis instance to use (supports transaction builders also)
   * @param key (the host name to remove expired names from)
   * @param time (the current time)
   * @param maxTimeDifference (the maximum time difference to expire by)
   * @return Future[Long] the result of the operation
   */
  private[this] def removeExpiredKeys(redis: RedisCommands, key: String, time: Long, maxTimeDifference: Long): Future[Long] = {
    val greaterThanTime = if (time - maxTimeDifference < 0) 0 else time - maxTimeDifference
    redis.zremrangebyscore(key, Limit(greaterThanTime, inclusive = false), Limit(time, inclusive = true))
  }

  /**
   * Saves the maximum number of times that a URL host can be called within a minute
   * @param redis the redis interface to use
   * @param url the url to rate limit
   * @param maxNumCalls the maximum number of calls per minute to rate limit the url to
   * @return a message indicating if the save succeeded or failed
   */
  def saveMaxNumberOfCalls(redis: RedisCommands, url: URL, maxNumCalls: Int): Future[RateLimiterMessage.Message] = {
    val host = url.getHost
    redis.set("config:" + host, maxNumCalls) onComplete {
      case Success(success) if success =>
        Future { RateLimiterMessage.ConfigSaved }
      case _ =>
        Future { RateLimiterMessage.ConfigFailed }
    }
  }

  /**
   * Retrieves the rate limit number for a url host or none if there is none
   * @param redis the redis interface to use
   * @param url the url to retrieve the rate limit for
   * @return an option of either the rate limit number or none
   */
  def getMaxNumberOfCalls(redis: RedisCommands, url: URL): Future[Option[Int]] = {
    redis.get("config:" + url.getHost) andThen {
      case Success(Some(data: ByteString)) =>
        val maxNumCalls: Int = RedisProtocolReply.decodeInteger(data) match {
          case Some((i, _)) => i.toInt
          case None => Int.MaxValue
        }
        Future { Some(maxNumCalls) }
      case _ => Future { None }
    }
  }

  /**
   * Checks the rate limit to see if a url can be called at a certain time
   * @param redis the redis client to use
   * @param url the url to check the rate limit for
   * @return a message indicating if you can or cannot call the url
   */
  def checkRateLimit(redis: RedisClient, url: URL): Future[RateLimiterMessage.Message] = {
    val host = url.getHost
    val getMaxNumberCalls = this.getMaxNumberOfCalls(redis, url)

    getMaxNumberCalls onSuccess {
      case Some(maxNumCalls) =>
        val transaction = redis.multi()
        val currentTime = System.currentTimeMillis()

        this.removeExpiredKeys(transaction, host, currentTime, MAX_TIME_DIFFERENCE)
        transaction.zadd(host, currentTime.toDouble -> currentTime)
        transaction.zcount(host)

        transaction.exec() andThen {
          case Success(MultiBulk(Some(responses))) =>
            val setSize = responses.last.asInstanceOf[Long]
            if (setSize > maxNumCalls) {
              Future { RateLimiterMessage.CannotCall }
            } else {
              Future { RateLimiterMessage.CanCall }
            }
          case Failure(e) =>
            Future { RateLimiterMessage.RedisError(e) }
        }
    }

    getMaxNumberCalls onFailure { case e =>
      Future { RateLimiterMessage.RedisError(e) }
    }
  }
}


