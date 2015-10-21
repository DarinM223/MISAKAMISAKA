import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.d_m.dns_resolver.DNSResolverActor
import com.typesafe.config.ConfigFactory
import org.scalatest._

/**
 * Created by darin on 10/21/15.
 */
class DNSResolverSpec
    extends TestKit(ActorSystem("DNSResolverSpec", ConfigFactory.parseString(DNSResolverSpec.config)))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers {

  val dnsResolverRef = system.actorOf(Props[DNSResolverActor], "DNSResolverActor")

  "A DNS Resolver" should {
    "Respond with TODO message when sent a string" in {
      dnsResolverRef ! "Hello world"
      expectMsg("TODO: send IP Address of domain name")
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
