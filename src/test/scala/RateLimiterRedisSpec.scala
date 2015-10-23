import java.net.URL

import com.d_m.rate_limiter.{Message, RedisUtils}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by darin on 10/23/15.
 */
class RateLimiterRedisSpec
    extends WordSpec
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient()

  override def afterAll(): Unit = {
    redis.flushall()
  }

  "saveMaxNumberOfCalls" should {
    "set the max number of calls in redis" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.google.com/calendar"), 5)
      whenReady(result) { value =>
        value should equal(Message.ConfigSaved)

        val getResult = redis.get[String]("config:www.google.com")
        whenReady(getResult) { case Some(value) =>
          value.toInt should equal(5)
        }
      }
    }
  }

  "getMaxNumberOfCalls" should {
    "get the max number of calls after setting" in {
      val result = RedisUtils.saveMaxNumberOfCalls(redis, new URL("http://www.google.com/maps"), 5) flatMap { case _ =>
        RedisUtils.getMaxNumberOfCalls(redis, new URL("http://www.google.com/maps"))
      }

      whenReady(result) { value =>
        value should equal(Some(5))
      }
    }

    "return None if value not set" in {
      val result = RedisUtils.getMaxNumberOfCalls(redis, new URL("http://www.facebook.com"))
      whenReady(result) { value =>
        value should equal(None)
      }
    }
  }
}
