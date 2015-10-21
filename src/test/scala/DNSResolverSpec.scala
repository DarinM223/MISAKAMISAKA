import java.net.InetAddress

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.dns_resolver.DNSResolverActor
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.xbill.DNS.Address

/**
 * Created by darin on 10/21/15.
 */
class DNSResolverSpec
    extends TestKit(ActorSystem("DNSResolverSpec", ConfigFactory.parseString(DNSResolverSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers {

  val dnsResolverRef = system.actorOf(Props(new DNSResolverActor(self)), "DNSResolverActor")

  "A DNS Resolver" should {
    "Respond with the resolved ip address when sent a url" in {
      val url = "www.google.com"
      dnsResolverRef ! url
      expectMsg(Address.getByName(url).getHostAddress)
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
