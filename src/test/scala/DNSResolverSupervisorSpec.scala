import java.net.URL
import java.util.regex.Pattern

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.dns_resolver.actors.DNSResolverSupervisor
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
class DNSResolverSupervisorSpec
  extends TestKit(ActorSystem("DNSResolverSupervisorSpec", ConfigFactory.parseString(DNSResolverSupervisorSpec.config)))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with ScalaFutures
  with BeforeAndAfterAll
  with Matchers {

  val redis = RedisClient(db = Some(4))
  val dnsResolverSupervisorRef = system.actorOf(Props(new DNSResolverSupervisor(redis)), "TestDNSResolverSupervisor")

  override def afterAll(): Unit = {
    redis.flushdb()
  }

  "A DNS Resolver supervisor" should {
    "allow ask query syntax" in {
      val dnsQuery = dnsResolverSupervisorRef ? (new URL("http://www.google.com"))

      val ipAddress = Await.result(dnsQuery, 1 second)

      val pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")
      pattern.matcher(ipAddress.toString).matches() should equal(true)
    }
  }
}

object DNSResolverSupervisorSpec {
  val config =
    """
      akka {
        loglevel = "WARNING"
      }
    """.stripMargin
}