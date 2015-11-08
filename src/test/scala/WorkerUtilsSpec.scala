import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures

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

  "WorkerUtils" should {
    "have true equal true" in {
      true should equal(true)
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