import java.net.URL

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.dns_resolver.actors.DNSResolverSupervisor
import com.d_m.rate_limiter.Message
import com.d_m.rate_limiter.actors.{RateLimiterSupervisor, RateLimiter}
import com.d_m.worker.WorkerUtils
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import redis.RedisClient
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

/**
 * Created by darin on 11/3/15.
 */
class WorkerUtilsSpec
    extends TestKit(ActorSystem("WorkerUtilsSpec", ConfigFactory.parseString(WorkerUtilsSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  // TODO(DarinM223): Supervisors conflict with each other on a single Redis instance. Are keys conflicting?
  val redis = RedisClient(db = Some(5))

  val dnsResolverSupervisor = system.actorOf(Props(new DNSResolverSupervisor(redis)), "TestDNSResolverSupervisorInWorkerUtils")
  val rateLimiterSupervisor = system.actorOf(Props(new RateLimiterSupervisor(redis)), "TestRateLimiterSupervisorInWorkerUtils")

  "getAddressAndRateLimit" should {
    "Get the address and rate limit for www.google.com" in {
      val result = (rateLimiterSupervisor ? (new URL("http://www.google.com"), 5)) flatMap {
        case _ =>
          WorkerUtils.getAddressAndRateLimit(new URL("http://www.google.com"), dnsResolverSupervisor, rateLimiterSupervisor)
      }

      val value = Await.result(result, 2 seconds)
      value._2 should equal(Message.CanCall)
    }
  }
}

object WorkerUtilsSpec {
  val config =
    """
      akka {
        loglevel = "WARNING"
      }
    """.stripMargin
}