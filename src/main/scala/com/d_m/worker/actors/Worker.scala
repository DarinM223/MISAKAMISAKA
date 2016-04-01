package com.d_m.worker.actors

import java.net.URL
import akka.actor._
import com.d_m.rate_limiter.Message
import com.d_m.worker.WorkerUtils
import spray.http.HttpResponse
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Actor for a worker that requests a web page, parses the links,
 * and sends the new links back to the supervisor
 */
class Worker(dnsResolver: ActorRef, rateLimiter: ActorRef) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case urlStr: String =>
      val url = new URL(urlStr)

      WorkerUtils.getAddressAndRateLimit(url, dnsResolver, rateLimiter) onComplete {
        case Success((address, result)) =>
          Worker.routeResult(sender, url, address, result)
        case Failure(e) =>
          sender ! com.d_m.worker.Message.RateLimitFailed(url)
      }
  }
}

object Worker {
  case class WorkerException(message: String) extends Exception(message)

  /**
   * Routes the result of fetching the DNS Resolved address and the Rate Limit return value
   * It then checks if the Rate Limit is not exceeded and if so it sends a request and calls
   * parseHttpBody on the result, otherwise it sends back a com.d_m.worker.Message.RateLimitFailed
   * back to the supervisor
   * @param url the url of the main link being parsed right now
   * @param address the dns resolved address of the url
   * @param result the result of the rate limiter query
   */
  def routeResult(sender: ActorRef, url: URL, address: String, result: Message.Message): Unit = result match {
    case Message.CanCall =>
      WorkerUtils.sendRequest(address) onComplete {
        case Success(response: HttpResponse) =>
          sender ! WorkerUtils.parseHttpBody(sender, response)
        case Failure(e) =>
          sender ! com.d_m.worker.Message.RateLimitFailed(url)
      }
    case Message.CannotCall =>
      sender ! com.d_m.worker.Message.RateLimitFailed(url)
    case _ => throw new WorkerException("Can only route CanCall and CannotCall messages")
  }
}
