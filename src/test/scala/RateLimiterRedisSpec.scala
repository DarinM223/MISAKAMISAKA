import java.net.URL

import com.d_m.rate_limiter.{Message, RedisUtils}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{WordSpec, BeforeAndAfterAll, Matchers}
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by darin on 10/23/15.
 */
class RateLimiterRedisSpec
    extends WordSpec
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient(db = Some(0))

  override def afterAll(): Unit = {
    redis.flushdb()
  }

  "saveMaxNumberOfCalls" should {
    "set the max number of calls in redis" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.google.com/calendar"), 5)
      val value = Await.result(result, 1 second)

      value should equal(Message.ConfigSaved)
      val getResult = redis.get[String]("config:www.google.com")
      val Some(compareValue) = Await.result(getResult, 1 second)

      compareValue.toInt should equal(5)
    }
  }

  "getMaxNumberOfCalls" should {
    "get the max number of calls after setting" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.google.com/maps"), 5) flatMap { _ =>
        RedisUtils.getMaxNumberOfCalls(redis, new URL("http://www.google.com/maps"))
      }

      Await.result(result, 1 second) should equal(Some(5))
    }

    "return None if value not set" in {
      val result = RedisUtils.getMaxNumberOfCalls(redis, new URL("http://www.facebook.com"))
      Await.result(result, 1 second) should equal(None)
    }
  }

  "checkRateLimit" should {
    "call once successfully" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.reddit.com"), 5) flatMap { _ =>
        val rateLimitRequest = RedisUtils.checkRateLimit(redis, new URL("http://www.reddit.com"))
        rateLimitRequest
      }

      val value = Await.result(result, 1 second)
      value.count(_ => true) should equal(1)
      value should equal(Some(Message.CanCall))
    }

    "call twitter 5 times in a second" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.twitter.com"), 5) flatMap { success =>
        val rateLimitRequests = (1 to 5).map(_ => RedisUtils.checkRateLimit(redis, new URL("http://www.twitter.com")))
        Future.sequence(rateLimitRequests)
      }

      val value = Await.result(result, 1 second)
      value.count(_ => true) should equal(5)
      value.foreach(_ should equal(Some(Message.CanCall)))
    }

    "calling linkedin 6 times in a second with a rate limit of 5 should cause an error" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.linkedin.com"), 5) flatMap { success =>
        val rateLimitRequests = (1 to 6).map(_ => RedisUtils.checkRateLimit(redis, new URL("http://www.linkedin.com")))
        Future.sequence(rateLimitRequests)
      }

      val value = Await.result(result, 1 second)
      value.count(_ => true) should equal(6)
      value.last should equal(Some(Message.CannotCall))
      (0 until 5).foreach(i => value(i) should equal(Some(Message.CanCall)))
    }
  }
}
