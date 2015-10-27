import java.net.URL

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.rate_limiter.Message
import com.d_m.rate_limiter.actors.RateLimiterSupervisor
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import redis.RedisClient
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by darin on 10/26/15.
 */
class RateLimiterSupervisorSpec
    extends TestKit(ActorSystem("RateLimiterSupervisorSpec", ConfigFactory.parseString(RateLimiterSupervisorSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  val redis = RedisClient(db = Some(3))
  val rateLimiterSupervisorRef = system.actorOf(Props[RateLimiterSupervisor], "RateLimiterSupervisor")

  override def afterAll(): Unit = {
    redis.flushdb()
  }

  "A rate limiter supervisor" should {
    "allow ask query syntax" in {
      val resultSave = rateLimiterSupervisorRef ? (new URL("http://www.google.com"), 5)
      Await.result(resultSave, 1 second) should equal(Message.ConfigSaved)

      val resultCheck = rateLimiterSupervisorRef ? new URL("http://www.google.com")
      Await.result(resultCheck, 1 second) should equal(Some(Message.CanCall))
    }
  }
}

object RateLimiterSupervisorSpec {
  val config =
    """
      akka {
        loglevel = "WARNING"
      }
    """.stripMargin
}