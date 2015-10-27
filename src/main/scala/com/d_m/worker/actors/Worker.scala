package com.d_m.worker.actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.d_m.rate_limiter.Message
import akka.io.IO
import spray.can.Http
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.HttpResponse
import spray.httpx.RequestBuilding._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by darin on 10/20/15.
 */
class Worker(dnsResolver: ActorRef, rateLimiter: ActorRef) extends Actor {
  def receive = {
    case urlStr: String =>
      val url = new URL(urlStr)

      Worker.getAddressAndRateLimit(url, dnsResolver, rateLimiter) onComplete {
        case Success((address, result)) =>
          Worker.routeResult(sender, url, address, result)
        case Failure(e) =>
          sender ! com.d_m.worker.Message.RateLimitFailed(url)
      }
  }
}

object Worker {
  // Implicit values for Akka IO
  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.MINUTES))
  implicit val system = ActorSystem()

  /**
   * Queries both the DNS Resolver and the Rate Limiter concurrently
   * @param url the url to resolve and check rate limit for
   * @return a future containing a tuple of the resolved address and the result of the rate limiter query
   */
  def getAddressAndRateLimit(url: URL, dnsResolver: ActorRef, rateLimiter: ActorRef): Future[(String, Message.Message)] = {
    val getAddressFuture: Future[String] = (dnsResolver ? url).mapTo[String]
    val getRateLimiterResult: Future[Message.Message] = (rateLimiter ? url).mapTo[Message.Message]

    // Fetch both results concurrently
    for {
      address: String <- getAddressFuture
      result: Message.Message <- getRateLimiterResult
    } yield (address, result)
  }

  /**
   * Sends a GET http request to an address
   * @param address the address to request
   * @return a future containing the response
   */
  def sendRequest(address: String): Future[HttpResponse] =
    (IO(Http) ? Get(address)).mapTo[HttpResponse]

  /**
   * Given a http response body, returns a list of links in the body
   * @param body the http response body
   * @return list of links in the body
   */
  def retrieveLinksFromBody(body: String): List[String] = List("TODO")

  /**
   * Unwraps the
   * @param response
   */
  def parseHttpBody(sender: ActorRef, response: HttpResponse) = response.entity match {
    case body: NonEmpty =>
      val links = retrieveLinksFromBody(body.asString)
      sender ! links
    case Empty =>
      sender ! List()
  }

  /**
   * Routes the result of fetching the DNS Resolved address and the Rate Limit return value
   * It then checks if the Rate Limit is not exceeded and if so it sends a request and calls
   * parseHttpBody on the result, otherwise it sends back a com.d_m.worker.Message.RateLimitFailed
   * back to the supervisor
   * @param url the url of the main link being parsed right now
   * @param address the dns resolved address of the url
   * @param result the result of the rate limiter query
   */
  def routeResult(sender: ActorRef, url: URL, address: String, result: Message.Message) = result match {
    case Message.CanCall =>
      sendRequest(address) onComplete {
        case Success(response: HttpResponse) =>
          parseHttpBody(sender, response)
        case Failure(e) =>
          sender ! com.d_m.worker.Message.RateLimitFailed(url)
      }
    case Message.CannotCall =>
      sender ! com.d_m.worker.Message.RateLimitFailed(url)
  }
}
