import java.net.URL

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.dns_resolver.DNSResolverActor
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import redis.RedisClient

/**
 * Created by darin on 10/21/15.
 */
class DNSResolverSpec
    extends TestKit(ActorSystem("DNSResolverSpec", ConfigFactory.parseString(DNSResolverSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with ScalaFutures
    with BeforeAndAfterAll
    with Matchers {

  val redis = RedisClient()
  val dnsResolverRef = system.actorOf(Props(new DNSResolverActor(self, redis)), "DNSResolverActor")

  override def afterAll(): Unit = {
    redis.flushall()
  }

  "A DNS Resolver" should {
    "Respond with the resolved ip address when sent a url and store the value in redis" in {
      val url = new URL("http://www.google.com")
      dnsResolverRef ! url
      expectMsgPF() {
        case _: String =>
          redis.get[String]("www.google.com").futureValue should not equal None
          ()
      }
    }

    "Return the cached value the second time" in {
      val url = new URL("http://www.facebook.com")
      dnsResolverRef ! url
      expectMsgPF() {
        case address: String =>
          dnsResolverRef ! url
          expectMsgPF() {
            case address2: String =>
              address should equal(address2)
              ()
          }
          ()
      }
    }
  }
}

object DNSResolverSpec {
  val config =
    """
      akka {
        loglevel = "WARNING"
      }
    """.stripMargin
}
