import java.net.URL

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.rate_limiter.Message
import com.d_m.rate_limiter.actors.RateLimiter
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import redis.RedisClient

/**
 * Created by darin on 10/23/15.
 */
class RateLimiterSpec
    extends TestKit(ActorSystem("RateLimiterSpec", ConfigFactory.parseString(RateLimiterSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  val redis = RedisClient(db = Some(2))
  val rateLimiterRef = system.actorOf(Props(new RateLimiter(redis)), "RateLimiterActor")

  override def afterAll(): Unit = {
    redis.flushdb()
  }

  "A rate limiter" should {
    "save the max number of calls" in {
      rateLimiterRef ! (self, new URL("http://www.google.com"), 5)

      expectMsgPF() {
        case Message.ConfigSaved =>
          redis.get[String]("config:www.google.com").futureValue should equal(Some("5"))
        case Message.ConfigFailed =>
          false should equal(true)
      }
    }

    "call twitter 5 times in a second" in {
      rateLimiterRef ! (self, new URL("http://www.twitter.com"), 5)
      expectMsg(Message.ConfigSaved)

      rateLimiterRef ! (self, new URL("http://www.twitter.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.twitter.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.twitter.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.twitter.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.twitter.com"))
      expectMsg(Some(Message.CanCall))
    }

    "should hit the rate limit by calling linkedin 6 times when rate limit is 5" in {
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"), 5)
      expectMsg(Message.ConfigSaved)

      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
    expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
      expectMsg(Some(Message.CanCall))
      rateLimiterRef ! (self, new URL("http://www.linkedin.com"))
      expectMsg(Some(Message.CannotCall))
    }
  }
}

object RateLimiterSpec {
  val config =
    """
      akka {
        loglevel = "WARNING"
      }
    """.stripMargin
}
