package com.d_m.rate_limiter

import java.math.BigInteger
import java.net.URL
import java.security.SecureRandom

import akka.util.ByteString
import redis.{RedisClient, RedisCommands}
import redis.api.Limit
import redis.protocol.{RedisReply, ParseNumber, MultiBulk}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Utility object for RateLimiter with functions
  * that use Redis to check rate limits
  */
object RedisUtils {
  // 1 second in nanoseconds
  val MAX_TIME_DIFFERENCE = 1000000000

  /**
    * Sets a unique element in a sorted set in redis for a certain key
    * Generates random hash, and attempts to add it
    * If there is no duplicate hash, then it should succeed otherwise it fails
    * @param redis the redis interface to use
    * @param key the host name to add a time to
    * @param time the time to add to a sorted set
    * @return
    */
  def addUniqueKey(
      redis: RedisCommands, key: String, time: Long): Future[Boolean] = {
    val random = new SecureRandom()
    val randomString = new BigInteger(130, random).toString

    redis.zadd(key, time.toDouble -> randomString) flatMap {
      case value: Long if value == 1 => Future { true }
      case _ => Future { false }
    }
  }

  /**
    * Removes all expired keys from a time and a time range
    * @param redis the redis instance to use (supports transaction builders also)
    * @param key (the host name to remove expired names from)
    * @param time (the current time)
    * @param maxTimeDifference (the maximum time difference to expire by)
    * @return Future[Long] the result of the operation
    */
  private[this] def removeExpiredKeys(
      redis: RedisCommands,
      key: String,
      time: Long,
      maxTimeDifference: Long): Future[Long] = {
    val greaterThanTime =
      if (time - maxTimeDifference < 0) 0 else time - maxTimeDifference
    redis.zremrangebyscore(key,
                           Limit(Double.NegativeInfinity, inclusive = true),
                           Limit(greaterThanTime, inclusive = false))
  }

  /**
    * Saves the maximum number of times that a URL host can be called within a minute
    * @param redis the redis interface to use
    * @param url the url to rate limit
    * @param maxNumCalls the maximum number of calls per minute to rate limit the url to
    * @return a message indicating if the save succeeded or failed
    */
  def saveMaxNumberOfCalls(redis: RedisCommands,
                           url: URL,
                           maxNumCalls: Int): Future[Message.Message] = {
    val host = url.getHost
    redis.set("config:" + host, maxNumCalls.toString) flatMap {
      case success if success => Future { Message.ConfigSaved }
      case _ => Future { Message.ConfigFailed }
    }
  }

  /**
    * Retrieves the rate limit number for a url host or none if there is none
    * @param redis the redis interface to use
    * @param url the url to retrieve the rate limit for
    * @return an option of either the rate limit number or none
    */
  def getMaxNumberOfCalls(
      redis: RedisCommands, url: URL): Future[Option[Int]] = {
    redis.get[String]("config:" + url.getHost) flatMap {
      case Some(str) => Future { Some(str.toInt) }
      case _ => Future { None }
    }
  }

  /**
    * Parses the Redis responses and returns a Message to be sent back
    * @param responses the responses to the Redis commands
    * @return a Message to be sent back
    */
  def unpackResponses(responses: Seq[RedisReply]): Option[Message.Message] = {
    // Unpack numbers from ByteString
    val addSuccess = ParseNumber.parseInt(
        responses(2).asOptByteString getOrElse ByteString("0"))

    // Fail if it failed to add the extra key
    if (addSuccess != 1) {
      None
    } else {
      // Unpack numbers from ByteString
      val maxNumCalls = ParseNumber.parseInt(
          responses.head.asOptByteString getOrElse ByteString("0"))
      val setSize = ParseNumber.parseInt(
          responses.last.asOptByteString getOrElse ByteString("0"))

      if (setSize > maxNumCalls) {
        Some(Message.CannotCall)
      } else {
        Some(Message.CanCall)
      }
    }
  }

  /**
    * Checks the rate limit to see if a url can be called at a certain time
    * @param redis the redis client to use
    * @param url the url to check the rate limit for
    * @return a message indicating if you can or cannot call the url
    */
  def checkRateLimit(
      redis: RedisClient, url: URL): Future[Option[Message.Message]] = {
    val host = url.getHost
    val currentTime = System.nanoTime()

    val transaction = redis.multi()

    this.getMaxNumberOfCalls(transaction, url)
    this.removeExpiredKeys(
        transaction, "ratelimit:" + host, currentTime, MAX_TIME_DIFFERENCE)
    this.addUniqueKey(transaction, "ratelimit:" + host, currentTime)
    transaction.zcount("ratelimit:" + host,
                       Limit(Double.NegativeInfinity),
                       Limit(Double.PositiveInfinity))

    val result = transaction.exec()

    result flatMap {
      case MultiBulk(Some(responses)) =>
        Future { this.unpackResponses(responses) }
      case _ => Future { None }
    }
  }
}
