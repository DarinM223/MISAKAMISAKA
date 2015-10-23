package com.d_m.rate_limiter

import redis.RedisCommands
import redis.api.Limit

import scala.concurrent.Future

/**
 * Created by darin on 10/22/15.
 */
object RateLimiterRedis {
  /**
   * Removes all expired keys from a time and a time range
   * @param redis the redis instance to use (supports transaction builders also)
   * @param key (the host name to remove expired names from)
   * @param time (the current time)
   * @param maxTimeDifference (the maximum time difference to expire by)
   * @return Future[Long] the result of the operation
   */
  def removeExpiredKeys(redis: RedisCommands, key: String, time: Long, maxTimeDifference: Long): Future[Long] = {
    val currentTime = System.currentTimeMillis()
    val greaterThanTime = if (currentTime - maxTimeDifference < 0) 0 else currentTime - maxTimeDifference
    redis.zremrangebyscore(key, Limit(greaterThanTime, inclusive = false), Limit(currentTime, inclusive = true))
  }
}
